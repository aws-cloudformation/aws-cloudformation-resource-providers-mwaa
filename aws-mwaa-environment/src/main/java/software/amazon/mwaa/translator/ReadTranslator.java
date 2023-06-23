// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.translator;

import static software.amazon.mwaa.TagProcessor.removeInternalTags;
import static software.amazon.mwaa.translator.TypeTranslator.toCfnLoggingConfiguration;
import static software.amazon.mwaa.translator.TypeTranslator.toCfnNetworkConfiguration;
import static software.amazon.mwaa.translator.TypeTranslator.toStringToObjectMap;

import software.amazon.awssdk.services.mwaa.model.Environment;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentResponse;
import software.amazon.mwaa.environment.ResourceModel;

/**
 * Provides translation between resource model and Read request/response structures.
 */
public final class ReadTranslator {

    private ReadTranslator() {
    }

    /**
     * Request to read a resource by model.
     *
     * @param model
     *         resource model
     * @return awsRequest the aws service request to describe a resource
     */
    public static GetEnvironmentRequest translateToReadRequest(final ResourceModel model) {
        return translateToReadRequest(model.getName());
    }

    /**
     * Request to read a resource by name.
     *
     * @param name
     *         environment name
     * @return awsRequest the aws service request to describe a resource
     */
    public static GetEnvironmentRequest translateToReadRequest(final String name) {
        return GetEnvironmentRequest.builder().name(name).build();
    }

    /**
     * Translates resource object from sdk into a resource model.
     *
     * @param awsResponse
     *         the aws service describe resource response
     * @return model resource model
     */
    public static ResourceModel translateFromReadResponse(final GetEnvironmentResponse awsResponse) {
        final Environment env = awsResponse.environment();

        return ResourceModel.builder()
                .name(env.name())
                .arn(env.arn())
                .executionRoleArn(env.executionRoleArn())
                .kmsKey(env.kmsKey())
                .airflowVersion(env.airflowVersion())
                .sourceBucketArn(env.sourceBucketArn())
                .dagS3Path(env.dagS3Path())
                .pluginsS3Path(env.pluginsS3Path())
                .pluginsS3ObjectVersion(env.pluginsS3ObjectVersion())
                .requirementsS3Path(env.requirementsS3Path())
                .requirementsS3ObjectVersion(env.requirementsS3ObjectVersion())
                .startupScriptS3Path(env.startupScriptS3Path())
                .startupScriptS3ObjectVersion(env.startupScriptS3ObjectVersion())
                .airflowConfigurationOptions(toStringToObjectMap(env.airflowConfigurationOptions()))
                .environmentClass(env.environmentClass())
                .maxWorkers(env.maxWorkers())
                .minWorkers(env.minWorkers())
                .schedulers(env.schedulers())
                .networkConfiguration(toCfnNetworkConfiguration(env.networkConfiguration()))
                .loggingConfiguration(toCfnLoggingConfiguration(env.loggingConfiguration()))
                .weeklyMaintenanceWindowStart(env.weeklyMaintenanceWindowStart())
                .tags(toStringToObjectMap(removeInternalTags(env.tags())))
                .webserverAccessMode(env.webserverAccessModeAsString())
                .webserverUrl(env.webserverUrl())
                .build();
    }
}
