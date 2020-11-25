// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.mwaa.Proxies;

/**
 * Handler for Read command.
 */
public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final Proxies proxies,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext) {

        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> getEnvironmentDetails("Read", proxies, progress));
    }
}
