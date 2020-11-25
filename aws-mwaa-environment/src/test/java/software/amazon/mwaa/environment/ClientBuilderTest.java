// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.mwaa.MwaaClient;

/**
 * Tests for {@link ClientBuilder}.
 */
class ClientBuilderTest {
    @Test
    public void getClientParameters() {
        // when
        final MwaaClient client = ClientBuilder.getClient("us-east-1");

        // then
        assertThat(client).isNotNull();
    }

    @Test
    public void getClientParametersForDev() {
        // when
        final MwaaClient client = ClientBuilder.getClient(null);

        // then
        assertThat(client).isNotNull();
    }
}
