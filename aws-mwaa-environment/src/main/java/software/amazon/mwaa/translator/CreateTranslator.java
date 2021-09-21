// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.translator;

import static software.amazon.mwaa.translator.TypeTranslator.toApiLoggingConfiguration;
import static software.amazon.mwaa.translator.TypeTranslator.toApiNetworkConfiguration;
import static software.amazon.mwaa.translator.TypeTranslator.toStringToStringMap;

import software.amazon.awssdk.services.mwaa.model.CreateEnvironmentRequest;
import software.amazon.mwaa.environment.ResourceModel;

/**
 * Provides translation between resource model and Create request/response structures.
 */
public final class CreateTranslator {

    private CreateTranslator() {
    }

    /**
     * Request to create a resource.
     *
     * @param model
     *         resource model
     * @return awsRequest the aws service request to create a resource
     */
    public static CreateEnvironmentRequest translateToCreateRequest(final ResourceModel model) {

        return CreateEnvironmentRequest.builder()
                .name(model.getName())
                .executionRoleArn(model.getExecutionRoleArn())
                .kmsKey(model.getKmsKey())
                .airflowVersion(model.getAirflowVersion())
                .sourceBucketArn(model.getSourceBucketArn())
                .dagS3Path(model.getDagS3Path())
                .pluginsS3Path(model.getPluginsS3Path())
                .pluginsS3ObjectVersion(
                        model.getPluginsS3ObjectVersion())
                .requirementsS3Path(model.getRequirementsS3Path())
                .requirementsS3ObjectVersion(
                        model.getRequirementsS3ObjectVersion())
                .airflowConfigurationOptions(toStringToStringMap(
                        model.getAirflowConfigurationOptions()))
                .environmentClass(model.getEnvironmentClass())
                .maxWorkers(model.getMaxWorkers())
                .minWorkers(model.getMinWorkers())
                .networkConfiguration(toApiNetworkConfiguration(
                        model.getNetworkConfiguration()))
                .loggingConfiguration(toApiLoggingConfiguration(
                        model.getLoggingConfiguration()))
                .weeklyMaintenanceWindowStart(
                        model.getWeeklyMaintenanceWindowStart())
                .tags(toStringToStringMap(model.getTags()))
                .webserverAccessMode(model.getWebserverAccessMode())
                .build();
    }
}
