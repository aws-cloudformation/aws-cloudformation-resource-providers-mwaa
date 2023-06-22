// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import software.amazon.awssdk.services.mwaa.MwaaClient;
import software.amazon.awssdk.services.mwaa.model.CreateEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.CreateEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.EnvironmentStatus;
import software.amazon.awssdk.services.mwaa.model.InternalServerException;
import software.amazon.awssdk.services.mwaa.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.mwaa.Proxies;
import software.amazon.mwaa.translator.CreateTranslator;
import software.amazon.mwaa.translator.ReadTranslator;

/**
 * Handler for Create command.
 */
public class CreateHandler extends BaseHandlerStd {
    private static final Duration CALLBACK_DELAY = Duration.ofMinutes(1);
    public static final int MAX_RETRIES = 14;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final Proxies proxies,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext) {

        final ResourceModel model = request.getDesiredResourceState();

        if (callbackContext.isStabilizing()) {
            log("callback context indicates Stabilizing mode");
            final Optional<EnvironmentStatus> status = getEnvironmentStatus(
                    proxies.getMwaaClientProxy(),
                    model.getName());

            if (status.isPresent()) {
                if (status.get() == EnvironmentStatus.AVAILABLE) {
                    log("status is AVAILABLE, returning success");
                    return ProgressEvent.progress(model, callbackContext).then(
                            progress -> getEnvironmentDetails("Create::PostCreateRead", proxies, progress));
                }

                if (status.get() == EnvironmentStatus.CREATE_FAILED) {
                    log("status is CREATE_FAILED, returning failure");
                    return ProgressEvent.failed(
                            model,
                            null,
                            HandlerErrorCode.NotStabilized,
                            "Creation failed");
                }
            }

            log("status is {}, requesting a callback in {}", status, CALLBACK_DELAY);
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .callbackContext(callbackContext)
                    .callbackDelaySeconds((int) CALLBACK_DELAY.getSeconds())
                    .status(OperationStatus.IN_PROGRESS)
                    .build();
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> startCreationTask(proxies, progress, callbackContext));
    }

    private ProgressEvent<ResourceModel, CallbackContext> startCreationTask(
            final Proxies proxies,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final CallbackContext callbackContext) {

        return startSubtask("Create", proxies, progress)
                .translateToServiceRequest(CreateTranslator::translateToCreateRequest)
                .makeServiceCall((awsRequest, mwaaClientProxy) ->
                                         doCreateEnvironment(awsRequest, mwaaClientProxy, callbackContext))
                .progress((int) CALLBACK_DELAY.getSeconds());
    }

    private CreateEnvironmentResponse doCreateEnvironment(
            final CreateEnvironmentRequest awsRequest,
            final ProxyClient<MwaaClient> mwaaClientProxy,
            final CallbackContext callbackContext) {

        final String name = awsRequest.name();
        log("Making sure %s does not exist", name);
        ensureEnvironmentDoesNotExist(ReadTranslator.translateToReadRequest(name), mwaaClientProxy);

        try {
            log("Creating %s [%s]", ResourceModel.TYPE_NAME, name);

            final CreateEnvironmentRetryListener listener = new CreateEnvironmentRetryListener(
                    getLogger(), MAX_RETRIES, name);
            final Retryer<CreateEnvironmentResponse> retryer = getCreateEnvironmentRetryer(listener);

            final CreateEnvironmentResponse response = retryer.call(() -> mwaaClientProxy.injectCredentialsAndInvokeV2(
                    awsRequest,
                    mwaaClientProxy.client()::createEnvironment));
            log("Create submitted %s [%s]", ResourceModel.TYPE_NAME, name);
            callbackContext.setStabilizing(true);
            return response;
        } catch (final RetryException e) {
            final Attempt<?> lastAttempt = e.getLastFailedAttempt();
            Throwable rootCause = lastAttempt.getExceptionCause();
            log("CreateEnvironment [%s]: Reached maximum number of retires. Total delay since first attempt: %dms",
                    name,
                    lastAttempt.getDelaySinceFirstAttempt());
            throw new CfnInvalidRequestException(rootCause.getMessage(), e);
        } catch (ExecutionException e) {
            throw new CfnInvalidRequestException(e.getCause().getMessage(), e);
        }
    }

    private Retryer<CreateEnvironmentResponse> getCreateEnvironmentRetryer(RetryListener listener) {
        return RetryerBuilder.<CreateEnvironmentResponse>newBuilder()
                .retryIfExceptionOfType(ValidationException.class)
                .retryIfExceptionOfType(InternalServerException.class)
                .withRetryListener(listener)
                .withWaitStrategy(WaitStrategies.exponentialWait())
                .withStopStrategy(StopStrategies.stopAfterAttempt(MAX_RETRIES))
                .build();
    }
}
