// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.mwaa.MwaaClient;

/**
 * Build SDK Client.
 */
public final class ClientBuilder {

    private ClientBuilder() {
    }

    /**
     * creates an SDK client.
     *
     * @param region
     *         AWS region for the request/session
     * @return a client which can be used to make API calls
     */
    public static MwaaClient getClient(final String region) {
        return MwaaClient.builder()
                .region(Region.of(region == null ? "us-west-2" : region))
                .build();
    }
}
