// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Configuration}.
 */
class ConfigurationTest {
    @Test
    public void schemaFilename() {
        // when
        final Configuration configuration = new Configuration();

        // then
        assertThat(configuration.schemaFilename).isEqualTo("aws-mwaa-environment.json");
    }
}
