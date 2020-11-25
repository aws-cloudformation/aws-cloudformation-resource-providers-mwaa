// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

/**
 * Provides schema configuration values.
 */
class Configuration extends BaseConfiguration {

    Configuration() {
        super("aws-mwaa-environment.json");
    }
}
