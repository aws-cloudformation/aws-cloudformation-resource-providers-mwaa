// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import java.time.Duration;
import java.util.Optional;
import software.amazon.awssdk.services.mwaa.MwaaClient;
import software.amazon.awssdk.services.mwaa.model.DeleteEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.DeleteEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.EnvironmentStatus;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.mwaa.Proxies;
import software.amazon.mwaa.translator.DeleteTranslator;
import software.amazon.mwaa.translator.ReadTranslator;

/**
 * Handler for Delete command.
 */
public class DeleteHandler extends BaseHandlerStd {
    private static final Duration CALLBACK_DELAY = Duration.ofMinutes(1);

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final Proxies proxies,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext) {

        final ResourceModel model = request.getDesiredResourceState();

        if (model == null) {
            return ProgressEvent.defaultSuccessHandler(null);
        }

        if (callbackContext.isStabilizing()) {
            log("callback context indicates Stabilizing mode");
            if (isEnvironmentDeleted(proxies, model)) {
                log("environment is Deleted, returning success");
                return ProgressEvent.defaultSuccessHandler(null);
            } else {
                log("environment is not Deleted, requesting a callback in {}", CALLBACK_DELAY);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .callbackContext(callbackContext)
                        .callbackDelaySeconds((int) CALLBACK_DELAY.getSeconds())
                        .status(OperationStatus.IN_PROGRESS)
                        .build();
            }
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> ensureEnvironmentExists(proxies, progress))
                .then(progress -> startDeleteTask(proxies, progress, callbackContext));
    }

    private ProgressEvent<ResourceModel, CallbackContext> ensureEnvironmentExists(
            final Proxies proxies,
            final ProgressEvent<ResourceModel, CallbackContext> progress) {

        return startSubtask("Delete::PreDeletionCheck", proxies, progress)
                .translateToServiceRequest(ReadTranslator::translateToReadRequest)
                .makeServiceCall(this::ensureEnvironmentExists)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> startDeleteTask(
            final Proxies proxies,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final CallbackContext callbackContext) {

        return startSubtask("Delete", proxies, progress)
                .translateToServiceRequest(DeleteTranslator::translateToDeleteRequest)
                .makeServiceCall((awsRequest, mwaaClientProxy) ->
                                         doDeleteEnvironment(awsRequest, mwaaClientProxy, callbackContext))
                .progress((int) CALLBACK_DELAY.getSeconds());
    }

    private DeleteEnvironmentResponse doDeleteEnvironment(
            final DeleteEnvironmentRequest awsRequest,
            final ProxyClient<MwaaClient> mwaaClientProxy,
            final CallbackContext callbackContext) {

        log("Deleting %s [%s]", ResourceModel.TYPE_NAME, awsRequest.name());

        final DeleteEnvironmentResponse response = mwaaClientProxy.injectCredentialsAndInvokeV2(
                awsRequest,
                mwaaClientProxy.client()::deleteEnvironment);

        log("Delete submitted %s [%s]", ResourceModel.TYPE_NAME, awsRequest.name());
        callbackContext.setStabilizing(true);
        return response;
    }

    protected boolean isEnvironmentDeleted(final Proxies proxies, final ResourceModel model) {
        final Optional<EnvironmentStatus> status =
                getEnvironmentStatus(proxies.getMwaaClientProxy(), model.getName());
        // consider a missing status as deleted
        return status.orElse(EnvironmentStatus.DELETED) == EnvironmentStatus.DELETED;
    }
}
