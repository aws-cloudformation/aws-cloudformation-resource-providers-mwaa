// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static software.amazon.mwaa.translator.ReadTranslator.translateFromReadResponse;
import static software.amazon.mwaa.translator.ReadTranslator.translateToReadRequest;

import java.util.Optional;
import software.amazon.awssdk.services.mwaa.MwaaClient;
import software.amazon.awssdk.services.mwaa.model.Environment;
import software.amazon.awssdk.services.mwaa.model.EnvironmentStatus;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.LastUpdate;
import software.amazon.awssdk.services.mwaa.model.ResourceNotFoundException;
import software.amazon.awssdk.services.mwaa.model.UpdateError;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.CallChain;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.mwaa.Proxies;
import software.amazon.mwaa.translator.ReadTranslator;

/**
 * Base handler which provides common functionalities for standard handlers.
 */
public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    private Logger logger;

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy awsClientProxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger requestLogger) {

        this.logger = requestLogger;
        final CallbackContext context = callbackContext != null ? callbackContext : new CallbackContext();

        final MwaaClient mwaaClient = ClientBuilder.getClient(request.getRegion());
        final ProxyClient<MwaaClient> mwaaClientProxy = awsClientProxy.newProxy(() -> mwaaClient);

        final Proxies proxies = Proxies.builder()
                .awsClientProxy(awsClientProxy)
                .mwaaClientProxy(mwaaClientProxy)
                .build();

        return handleRequest(proxies, request, context);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            Proxies proxies,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext);

    protected CallChain.RequestMaker<MwaaClient, ResourceModel, CallbackContext> startSubtask(
            String subtaskName,
            Proxies proxies,
            ProgressEvent<ResourceModel, CallbackContext> progress) {

        final ResourceModel model = progress.getResourceModel();
        final CallbackContext context = progress.getCallbackContext();
        final String fullTaskName = "AWS-MWAA-Environment::" + subtaskName;

        log("Starting %s", fullTaskName);

        return proxies.getAwsClientProxy().initiate(
                fullTaskName,
                proxies.getMwaaClientProxy(),
                model,
                context);
    }

    protected void log(final String format, final Object... args) {
        if (logger != null) {
            logger.log(String.format(format, args));
        }
    }

    protected Optional<EnvironmentStatus> getEnvironmentStatus(
            final ProxyClient<MwaaClient> mwaaClientProxy,
            final String name) {
        final GetEnvironmentRequest awsRequest = translateToReadRequest(name);
        return getEnvironmentStatus(mwaaClientProxy, awsRequest);
    }

    protected Optional<EnvironmentStatus> getEnvironmentStatus(
            final ProxyClient<MwaaClient> mwaaClientProxy,
            final GetEnvironmentRequest awsRequest) {

        try {
            final Environment environment = getEnvironment(mwaaClientProxy, awsRequest);
            final EnvironmentStatus status = EnvironmentStatus.fromValue(environment.statusAsString().toUpperCase());

            log("%s [%s] exists. Status: %s", ResourceModel.TYPE_NAME, environment.name(), status);
            return Optional.of(status);
        } catch (CfnNotFoundException e) {
            log("%s [%s] does not exist", ResourceModel.TYPE_NAME, awsRequest.name());
            return Optional.empty();
        }
    }


    protected Environment getEnvironment(final ProxyClient<MwaaClient> mwaaClientProxy,
                                         final GetEnvironmentRequest awsRequest) {
        final GetEnvironmentResponse response = doReadEnvironment(awsRequest, mwaaClientProxy);
        return response.environment();

    }

    protected Optional<UpdateError> getLastUpdateError(
            final ProxyClient<MwaaClient> mwaaClientProxy,
            final String name) {
        final GetEnvironmentRequest awsRequest = translateToReadRequest(name);
        try {
            final Environment environment = getEnvironment(mwaaClientProxy, awsRequest);
            final Optional<UpdateError> lastUpdateError = Optional.of(environment.lastUpdate())
                    .map(LastUpdate::error);
            return lastUpdateError;
        } catch (CfnNotFoundException e) {
            log("%s [%s] does not exist", ResourceModel.TYPE_NAME, awsRequest.name());
            return Optional.empty();
        }
    }

    protected ProgressEvent<ResourceModel, CallbackContext> ensureEnvironmentExists(
            final GetEnvironmentRequest awsRequest,
            final ProxyClient<MwaaClient> mwaaClientProxy) {

        final Optional<EnvironmentStatus> status = getEnvironmentStatus(mwaaClientProxy, awsRequest);
        if (status.isPresent()) {
            // null progress to indicate a no-op (OK situation)
            return null;
        }

        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.name());
    }

    protected ProgressEvent<ResourceModel, CallbackContext> ensureEnvironmentDoesNotExist(
            final GetEnvironmentRequest awsRequest,
            final ProxyClient<MwaaClient> mwaaClientProxy) {

        final Optional<EnvironmentStatus> status = getEnvironmentStatus(mwaaClientProxy, awsRequest);
        if (status.isPresent()) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME,
                                                awsRequest.name());
        }

        // null progress to indicate a no-op (OK situation)
        return null;
    }

    protected ProgressEvent<ResourceModel, CallbackContext> getEnvironmentDetails(
            final String taskName,
            final Proxies proxies,
            final ProgressEvent<ResourceModel, CallbackContext> progress) {

        return startSubtask(taskName, proxies, progress)
                .translateToServiceRequest(ReadTranslator::translateToReadRequest)
                .makeServiceCall(this::doReadEnvironment)
                .done(awsResponse -> ProgressEvent.defaultSuccessHandler(translateFromReadResponse(awsResponse)));
    }

    protected GetEnvironmentResponse doReadEnvironment(
            GetEnvironmentRequest request,
            ProxyClient<MwaaClient> mwaaClientProxy) {
        try {
            log("Getting %s [%s]", ResourceModel.TYPE_NAME, request.name());
            final GetEnvironmentResponse response = mwaaClientProxy.injectCredentialsAndInvokeV2(
                    request,
                    mwaaClientProxy.client()::getEnvironment);

            log("Got %s [%s]",
                ResourceModel.TYPE_NAME,
                response.environment().name());
            return response;
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.name(), e);
        }
    }
}
