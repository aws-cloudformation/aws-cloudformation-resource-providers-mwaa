// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.translator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.awssdk.services.mwaa.model.LoggingConfiguration;
import software.amazon.awssdk.services.mwaa.model.LoggingConfigurationInput;
import software.amazon.awssdk.services.mwaa.model.ModuleLoggingConfiguration;
import software.amazon.awssdk.services.mwaa.model.ModuleLoggingConfigurationInput;
import software.amazon.awssdk.services.mwaa.model.NetworkConfiguration;
import software.amazon.awssdk.services.mwaa.model.UpdateNetworkConfigurationInput;

/**
 * Provides translation between different types of CFN Model and SDK.
 */
public final class TypeTranslator {

    private TypeTranslator() {
    }

    /**
     * Converts a collection to a safe stream which is never null.
     *
     * @param collection
     *         a collection which can be null
     * @param <T>
     *         type of the collection entities
     * @return a stream
     */
    public static <T> Stream<T> toStreamOfOrEmpty(final Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }

    /**
     * Converts a Map from String to Objects to a Map from String to String.
     *
     * @param input
     *         String to Object Map
     * @return String to String Map
     */
    public static Map<String, String> toStringToStringMap(final Map<String, Object> input) {
        if (input == null) {
            return null;
        }

        Map<String, String> result = new HashMap<>();
        input.forEach((key, value) -> result.put(key, value == null ? null : value.toString()));
        return result;
    }

    /**
     * Converts a Map to a string which is suitable to display in logs.
     *
     * @param input
     *         Map
     * @return a String which can be displayed in log
     */
    public static String mapToLogString(Map<String, String> input) {
        if (input == null) {
            return "{}";
        }

        final String keyValues = input
                .keySet()
                .stream()
                .sorted()
                .map(key -> String.format("%s=%s", key, input.get(key) == null ? "null" : quote(input.get(key))))
                .collect(Collectors.joining(", "));
        return "{" + keyValues + "}";
    }

    /**
     * Converts a Collection to a string which is suitable to display in logs.
     *
     * @param input
     *         Collection
     * @return a String which can be displayed in log
     */
    public static String collectionToLogString(Collection<String> input) {
        if (input == null) {
            return "[]";
        }

        final String values = input
                .stream()
                .map(value -> value == null ? "null" : quote(value))
                .sorted()
                .collect(Collectors.joining(", "));
        return "[" + values + "]";
    }

    private static String quote(final String str) {
        return "\"" + str + "\"";
    }

    /**
     * Converts CFN NetworkConfiguration to API NetworkConfiguration.
     *
     * @param input
     *         CFN NetworkConfiguration
     * @return API NetworkConfiguration
     */
    public static NetworkConfiguration toApiNetworkConfiguration(
            final software.amazon.mwaa.environment.NetworkConfiguration input) {
        if (input == null) {
            return null;
        }

        return NetworkConfiguration.builder()
                .subnetIds(input.getSubnetIds())
                .securityGroupIds(input.getSecurityGroupIds())
                .build();
    }

    /**
     * Converts CFN NetworkConfiguration to API UpdateNetworkConfigurationInput.
     *
     * @param input
     *         CFN NetworkConfiguration
     * @return API UpdateNetworkConfigurationInput
     */
    public static UpdateNetworkConfigurationInput toApiUpdateNetworkConfiguration(
            final software.amazon.mwaa.environment.NetworkConfiguration input) {
        if (input == null) {
            return null;
        }

        return UpdateNetworkConfigurationInput.builder()
                .securityGroupIds(input.getSecurityGroupIds())
                .build();
    }

    /**
     * Converts CFN LoggingConfigurationInput to API LoggingConfigurationInput.
     *
     * @param input
     *         CFN LoggingConfigurationInput
     * @return API LoggingConfigurationInput
     */
    public static LoggingConfigurationInput toApiLoggingConfiguration(
            final software.amazon.mwaa.environment.LoggingConfiguration input) {
        if (input == null) {
            return null;
        }

        return LoggingConfigurationInput.builder()
                .dagProcessingLogs(toApiModuleLoggingConfigurationInput(input.getDagProcessingLogs()))
                .schedulerLogs(toApiModuleLoggingConfigurationInput(input.getSchedulerLogs()))
                .webserverLogs(toApiModuleLoggingConfigurationInput(input.getWebserverLogs()))
                .workerLogs(toApiModuleLoggingConfigurationInput(input.getWorkerLogs()))
                .taskLogs(toApiModuleLoggingConfigurationInput(input.getTaskLogs()))
                .build();
    }

    /**
     * Converts CFN ModuleLoggingConfiguration to API ModuleLoggingConfiguration.
     *
     * @param input
     *         CFN ModuleLoggingConfiguration
     * @return API ModuleLoggingConfiguration
     */
    public static ModuleLoggingConfigurationInput toApiModuleLoggingConfigurationInput(
            final software.amazon.mwaa.environment.ModuleLoggingConfiguration input) {
        if (input == null) {
            return null;
        }

        return ModuleLoggingConfigurationInput.builder()
                .enabled(input.getEnabled())
                .logLevel(input.getLogLevel())
                .build();
    }

    /**
     * Converts a Map from String to String to a Map from String to Object.
     *
     * @param input
     *         String to String Map
     * @return String to Object Map
     */
    public static Map<String, Object> toStringToObjectMap(final Map<String, String> input) {
        if (input == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        input.forEach(result::put);
        return result;
    }

    /**
     * Converts API NetworkConfiguration to CFN NetworkConfiguration.
     *
     * @param input
     *         API NetworkConfiguration
     * @return CFN NetworkConfiguration
     */
    public static software.amazon.mwaa.environment.NetworkConfiguration toCfnNetworkConfiguration(
            final NetworkConfiguration input) {
        if (input == null) {
            return null;
        }

        return software.amazon.mwaa.environment.NetworkConfiguration.builder()
                .securityGroupIds(input.securityGroupIds())
                .subnetIds(input.subnetIds())
                .build();
    }


    /**
     * Converts API LoggingConfiguration to CFN LoggingConfiguration.
     *
     * @param input
     *         API LoggingConfiguration
     * @return CFN LoggingConfiguration
     */
    public static software.amazon.mwaa.environment.LoggingConfiguration toCfnLoggingConfiguration(
            final LoggingConfiguration input) {
        if (input == null) {
            return null;
        }

        return software.amazon.mwaa.environment.LoggingConfiguration.builder()
                .dagProcessingLogs(toCfnModuleLoggingConfiguration(input.dagProcessingLogs()))
                .schedulerLogs(toCfnModuleLoggingConfiguration(input.schedulerLogs()))
                .webserverLogs(toCfnModuleLoggingConfiguration(input.webserverLogs()))
                .workerLogs(toCfnModuleLoggingConfiguration(input.workerLogs()))
                .taskLogs(toCfnModuleLoggingConfiguration(input.taskLogs()))
                .build();
    }

    /**
     * Converts API ModuleLoggingConfiguration to CFN ModuleLoggingConfiguration.
     *
     * @param input
     *         API ModuleLoggingConfiguration
     * @return CFN ModuleLoggingConfiguration
     */
    public static software.amazon.mwaa.environment.ModuleLoggingConfiguration toCfnModuleLoggingConfiguration(
            final ModuleLoggingConfiguration input) {
        if (input == null) {
            return null;
        }

        return software.amazon.mwaa.environment.ModuleLoggingConfiguration.builder()
                .enabled(input.enabled())
                .logLevel(input.logLevelAsString())
                .cloudWatchLogGroupArn(input.cloudWatchLogGroupArn())
                .build();
    }
}
