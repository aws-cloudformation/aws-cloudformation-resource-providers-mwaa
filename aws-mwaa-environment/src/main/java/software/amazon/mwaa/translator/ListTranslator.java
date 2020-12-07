// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.translator;

import static software.amazon.mwaa.translator.TypeTranslator.toStreamOfOrEmpty;

import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.mwaa.model.ListEnvironmentsRequest;
import software.amazon.awssdk.services.mwaa.model.ListEnvironmentsResponse;
import software.amazon.mwaa.environment.ResourceModel;

/**
 * Provides translation between resource model and List request/response structures.
 */
public final class ListTranslator {

    private ListTranslator() {
    }

    /**
     * Request to list resources.
     *
     * @param nextToken
     *         token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    public static ListEnvironmentsRequest translateToListRequest(final String nextToken) {
        return ListEnvironmentsRequest.builder().nextToken(nextToken).build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only).
     *
     * @param awsResponse
     *         the aws service describe resource response
     * @return list of resource models
     */
    public static List<ResourceModel> translateFromListResponse(final ListEnvironmentsResponse awsResponse) {
        return toStreamOfOrEmpty(awsResponse.environments())
                .map(name -> ResourceModel.builder().name(name).build())
                .collect(Collectors.toList());
    }
}
