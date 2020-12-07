// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.translator;

import static software.amazon.mwaa.translator.TypeTranslator.toApiLoggingConfiguration;
import static software.amazon.mwaa.translator.TypeTranslator.toStringToStringMap;

import software.amazon.awssdk.services.mwaa.model.UpdateEnvironmentRequest;
import software.amazon.mwaa.environment.ResourceModel;


/**
 * Provides translation between resource model and Update request/response structures.
 */
public final class UpdateTranslator {

    private UpdateTranslator() {
    }

    /**
     * Request to update properties of a previously created resource.
     *
     * @param model
     *         resource model
     * @return awsRequest the aws service request to modify a resource
     */
    public static UpdateEnvironmentRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateEnvironmentRequest.builder()
                .name(model.getName())
                .executionRoleArn(model.getExecutionRoleArn())
                .airflowVersion(model.getAirflowVersion())
                .sourceBucketArn(model.getSourceBucketArn())
                .dagS3Path(model.getDagS3Path())
                .pluginsS3Path(model.getPluginsS3Path())
                .pluginsS3ObjectVersion(model.getPluginsS3ObjectVersion())
                .requirementsS3Path(model.getRequirementsS3Path())
                .requirementsS3ObjectVersion(model.getRequirementsS3ObjectVersion())
                .airflowConfigurationOptions(toStringToStringMap(model.getAirflowConfigurationOptions()))
                .environmentClass(model.getEnvironmentClass())
                .maxWorkers(model.getMaxWorkers())
                .loggingConfiguration(toApiLoggingConfiguration(model.getLoggingConfiguration()))
                .weeklyMaintenanceWindowStart(model.getWeeklyMaintenanceWindowStart())
                .webserverAccessMode(model.getWebserverAccessMode())
                .build();
    }

}
