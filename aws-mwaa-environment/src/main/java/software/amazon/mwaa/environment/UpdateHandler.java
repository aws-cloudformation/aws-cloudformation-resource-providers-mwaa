// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static software.amazon.mwaa.translator.TypeTranslator.collectionToLogString;
import static software.amazon.mwaa.translator.TypeTranslator.mapToLogString;
import static software.amazon.mwaa.translator.TypeTranslator.toStringToStringMap;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import software.amazon.awssdk.services.mwaa.MwaaClient;
import software.amazon.awssdk.services.mwaa.model.Environment;
import software.amazon.awssdk.services.mwaa.model.EnvironmentStatus;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.ResourceNotFoundException;
import software.amazon.awssdk.services.mwaa.model.TagResourceRequest;
import software.amazon.awssdk.services.mwaa.model.UntagResourceRequest;
import software.amazon.awssdk.services.mwaa.model.UpdateEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.UpdateEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.UpdateError;
import software.amazon.awssdk.services.mwaa.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.mwaa.Proxies;
import software.amazon.mwaa.TagProcessor;
import software.amazon.mwaa.translator.ReadTranslator;
import software.amazon.mwaa.translator.UpdateTranslator;

/**
 * Handler for Update command.
 */
@SuppressWarnings({"checkstyle:MethodLength"})
public class UpdateHandler extends BaseHandlerStd {
    private static final Duration CALLBACK_DELAY = Duration.ofMinutes(1);

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

            final Optional<UpdateError> lastUpdateError = getLastUpdateError(
                    proxies.getMwaaClientProxy(),
                    model.getName());
            String errorMessage = lastUpdateError.map(UpdateError::errorMessage).orElse("");

            if (!status.isPresent()) {
                log("Environment not found, failing update");
                return ProgressEvent.failed(
                        model,
                        null,
                        HandlerErrorCode.NotStabilized,
                        "Update failed, resource no longer exists");
            }
            if (status.get() == EnvironmentStatus.AVAILABLE) {
                log("status is AVAILABLE, returning success");
                return ProgressEvent.progress(model, callbackContext).then(
                        progress -> getEnvironmentDetails("Update::PostUpdateRead", proxies, progress));
            }
            if (status.get() == EnvironmentStatus.UPDATE_FAILED) {
                  log("status is UPDATE_FAILED, returning failure");
                return ProgressEvent.failed(
                        model,
                        null,
                        HandlerErrorCode.NotStabilized,
                        String.format("Update failed. %s", errorMessage));
            }
            if (status.get() == EnvironmentStatus.UNAVAILABLE) {
                log("status is UNAVAILABLE, returning failure");
                return ProgressEvent.failed(
                        model,
                        null,
                        HandlerErrorCode.NotStabilized,
                        String.format("Update failed, Environment unavailable. %s", errorMessage));
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
                .then(progress -> startUpdateTask(proxies, progress, callbackContext));
    }

    private ProgressEvent<ResourceModel, CallbackContext> startUpdateTask(
            final Proxies proxies,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final CallbackContext callbackContext) {

        return startSubtask("Update", proxies, progress)
                .translateToServiceRequest(UpdateTranslator::translateToUpdateRequest)
                .makeServiceCall((awsRequest, mwaaClientProxy) -> doUpdateEnvironment(
                        awsRequest,
                        toStringToStringMap(progress.getResourceModel().getTags()),
                        mwaaClientProxy,
                        callbackContext))
                .progress((int) CALLBACK_DELAY.getSeconds());
    }

    private UpdateEnvironmentResponse doUpdateEnvironment(
            final UpdateEnvironmentRequest awsRequest,
            Map<String, String> desiredTags,
            final ProxyClient<MwaaClient> mwaaClientProxy,
            final CallbackContext callbackContext) {

        try {
            final String name = awsRequest.name();
            updateTags(mwaaClientProxy, name, desiredTags);

            log("Updating %s [%s]", ResourceModel.TYPE_NAME, name);

            final UpdateEnvironmentResponse response = mwaaClientProxy.injectCredentialsAndInvokeV2(
                    awsRequest,
                    mwaaClientProxy.client()::updateEnvironment);
            log("Update submitted %s [%s]", ResourceModel.TYPE_NAME, awsRequest.name());
            callbackContext.setStabilizing(true);
            return response;
        } catch (final ValidationException e) {
            throw new CfnInvalidRequestException(e.getMessage(), e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, awsRequest.name(), e);
        }
    }

    private void updateTags(ProxyClient<MwaaClient> mwaaClientProxy, String name, Map<String, String> desiredTags) {
        final Environment environment = getEnvironment(mwaaClientProxy, name);
        log("Tags of %s: %s", environment.arn(), mapToLogString(environment.tags()));

        final TagProcessor tagProcessor = new TagProcessor(environment.tags());
        removeTags(mwaaClientProxy, tagProcessor, environment.arn(), desiredTags);
        addTags(mwaaClientProxy, tagProcessor, environment.arn(), desiredTags);
    }

    private void removeTags(final ProxyClient<MwaaClient> mwaaClientProxy,
                            final TagProcessor tagProcessor,
                            final String arn,
                            final Map<String, String> desiredTags) {
        final Collection<String> tagsToRemove = tagProcessor.getTagsToRemove(desiredTags);
        log("Tags to remove: %s", collectionToLogString(tagsToRemove));
        if (tagsToRemove.isEmpty()) {
            return;
        }

        log("Untagging...");
        mwaaClientProxy.injectCredentialsAndInvokeV2(
                UntagResourceRequest.builder()
                        .resourceArn(arn)
                        .tagKeys(tagsToRemove)
                        .build(),
                mwaaClientProxy.client()::untagResource);
        log("Untagging done");
    }

    private void addTags(final ProxyClient<MwaaClient> mwaaClientProxy,
                         final TagProcessor tagProcessor,
                         final String arn,
                         final Map<String, String> desiredTags) {
        final Map<String, String> tagsToAdd = tagProcessor.getTagsToAdd(desiredTags);
        log("Tags to add: %s", mapToLogString(tagsToAdd));
        if (tagsToAdd.isEmpty()) {
            return;
        }

        log("Tagging...");
        mwaaClientProxy.injectCredentialsAndInvokeV2(
                TagResourceRequest.builder()
                        .resourceArn(arn)
                        .tags(tagsToAdd)
                        .build(),
                mwaaClientProxy.client()::tagResource);
        log("Tagging done");
    }

    private Environment getEnvironment(ProxyClient<MwaaClient> mwaaClientProxy, String name) {
        log("Getting information about %s", name);
        final GetEnvironmentRequest getEnvironmentRequest = ReadTranslator.translateToReadRequest(name);
        return doReadEnvironment(getEnvironmentRequest, mwaaClientProxy).environment();
    }
}
