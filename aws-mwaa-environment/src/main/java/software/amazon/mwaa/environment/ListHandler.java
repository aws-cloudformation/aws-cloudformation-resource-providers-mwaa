// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static software.amazon.mwaa.translator.ListTranslator.translateFromListResponse;
import static software.amazon.mwaa.translator.ListTranslator.translateToListRequest;

import java.util.List;
import software.amazon.awssdk.services.mwaa.model.ListEnvironmentsRequest;
import software.amazon.awssdk.services.mwaa.model.ListEnvironmentsResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.mwaa.Proxies;

/**
 * Handler for List command.
 */
public class ListHandler extends BaseHandlerStd {
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final Proxies proxies,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext) {

        final ListEnvironmentsRequest listEnvironmentsRequest = translateToListRequest(request.getNextToken());
        final ListEnvironmentsResponse response = proxies.getMwaaClientProxy().injectCredentialsAndInvokeV2(
                listEnvironmentsRequest,
                proxies.getMwaaClientProxy().client()::listEnvironments);

        final List<ResourceModel> models = translateFromListResponse(response);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(response.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
