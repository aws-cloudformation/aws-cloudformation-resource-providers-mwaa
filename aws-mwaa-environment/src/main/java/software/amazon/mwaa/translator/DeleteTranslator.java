// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.translator;

import software.amazon.awssdk.services.mwaa.model.DeleteEnvironmentRequest;
import software.amazon.mwaa.environment.ResourceModel;

/**
 * Provides translation between resource model and Delete request/response structures.
 */
public final class DeleteTranslator {

    private DeleteTranslator() {
    }

    /**
     * Request to delete a resource.
     *
     * @param model
     *         resource model
     * @return awsRequest the aws service request to delete a resource
     */
    public static DeleteEnvironmentRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteEnvironmentRequest.builder().name(model.getName()).build();
    }
}
