// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.mwaa.MwaaClient;
import software.amazon.awssdk.services.mwaa.model.EndpointManagement;
import software.amazon.awssdk.services.mwaa.model.Environment;
import software.amazon.awssdk.services.mwaa.model.EnvironmentStatus;
import software.amazon.awssdk.services.mwaa.model.WebserverAccessMode;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.mwaa.Proxies;

/**
 * Base for tests.
 */
public class HandlerTestBase {
    private static final String NAME = "NAME";
    private static final String EXECUTION_ROLE_ARN = "EXECUTION_ROLE_ARN";
    private static final String KMS_KEY = "KMS_KEY";
    private static final String AIRFLOW_VERSION = "AIRFLOW_VERSION";
    private static final String SOURCE_BUCKET_ARN = "SOURCE_BUCKET_ARN";
    private static final String DAG_S3_PATH = "DAG_S3_PATH";
    private static final String PLUGINS_S3_PATH = "PLUGINS_S3_PATH";
    private static final String PLUGINS_S3_OBJECT_VERSION = "PLUGINS_S3_OBJECT_VERSION";
    private static final String REQUIREMENTS_S3_PATH = "REQUIREMENTS_S3_PATH";
    private static final String REQUIREMENTS_S3_OBJECT_VERSION = "REQUIREMENTS_S3_OBJECT_VERSION";
    private static final String STARTUP_SCRIPT_S3_PATH = "STARTUP_SCRIPT_S3_PATH";
    private static final String STARTUP_SCRIPT_S3_OBJECT_VERSION = "STARTUP_SCRIPT_S3_OBJECT_VERSION";
    private static final String ENVIRONMENT_CLASS = "ENVIRONMENT_CLASS";
    private static final String WEEKLY_MAINTENANCE_WINDOW_START = "WEEKLY_MAINTENANCE_WINDOW_START";
    private static final Integer MAX_WORKERS = 3;
    private static final Integer MIN_WORKERS = 1;
    private static final Integer SCHEDULERS = 2;
    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static final String KEY_INTERNAL = "aws:tag:domain";
    private static final String VALUE_INTERNAL = "beta";
    private static final String SUBNET_ID_1 = "SUBNET_ID_1";
    private static final String SUBNET_ID_2 = "SUBNET_ID_2";
    private static final String SECURITY_GROUP_1 = "SECURITY_GROUP_1";
    private static final String SECURITY_GROUP_2 = "SECURITY_GROUP_2";
    private static final Boolean ENABLED = true;
    private static final Boolean DISABLED = false;
    private static final String LOG_LEVEL_1 = "LOG_LEVEL_1";
    private static final String LOG_LEVEL_2 = "LOG_LEVEL_2";
    private static final String LOG_LEVEL_3 = "LOG_LEVEL_3";
    private static final String LOG_LEVEL_4 = "LOG_LEVEL_4";
    private static final String LOG_LEVEL_5 = "LOG_LEVEL_5";
    private static final String ARN_1 = "ARN_1";
    private static final String ARN_2 = "ARN_2";
    private static final String ARN_3 = "ARN_3";
    private static final String ARN_4 = "ARN_4";
    private static final String ARN_5 = "ARN_5";
    private static final String PRIVATE_ONLY = "PRIVATE_ONLY";
    private static final String SERVICE = "SERVICE";
    private static final Duration CALLBACK_DELAY = Duration.ofMinutes(1);

    private static final int CLIENT_PROXY_TIMEOUT_SECONDS = 600;

    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy LOGGER;

    @Mock
    private AmazonWebServicesClientProxy awsClientProxy;

    @Mock
    private ProxyClient<MwaaClient> mwaaClientProxy;

    @Mock
    private MwaaClient sdkClient;

    private Proxies proxies;

    protected MwaaClient getSdkClient() {
        return sdkClient;
    }

    protected Proxies getProxies() {
        return proxies;
    }

    /**
     * Sets up mocks before each test.
     */
    @BeforeEach
    public void setupProxies() {
        awsClientProxy = new AmazonWebServicesClientProxy(
                LOGGER,
                MOCK_CREDENTIALS,
                () -> Duration.ofSeconds(CLIENT_PROXY_TIMEOUT_SECONDS).toMillis());
        sdkClient = mock(MwaaClient.class);
        mwaaClientProxy = mockProxy(awsClientProxy, sdkClient);
        proxies = Proxies.builder()
                .awsClientProxy(awsClientProxy)
                .mwaaClientProxy(mwaaClientProxy)
                .build();
    }

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        LOGGER = new LoggerProxy();
    }

    static ProxyClient<MwaaClient> mockProxy(
            final AmazonWebServicesClientProxy proxy,
            final MwaaClient sdkClient) {
        return new ProxyClient<MwaaClient>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
            injectCredentialsAndInvokeV2(
                    RequestT request,
                    Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            CompletableFuture<ResponseT>
            injectCredentialsAndInvokeV2Async(
                    RequestT request,
                    Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest,
                    ResponseT extends AwsResponse,
                    IterableT extends SdkIterable<ResponseT>>
            IterableT
            injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
            injectCredentialsAndInvokeV2InputStream(
                    RequestT requestT,
                    Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
            injectCredentialsAndInvokeV2Bytes(
                    RequestT requestT,
                    Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public MwaaClient client() {
                return sdkClient;
            }
        };
    }

    ResourceModel createCfnModel() {
        return ResourceModel.builder()
                .name(NAME)
                .executionRoleArn(EXECUTION_ROLE_ARN)
                .kmsKey(KMS_KEY)
                .airflowVersion(AIRFLOW_VERSION)
                .sourceBucketArn(SOURCE_BUCKET_ARN)
                .dagS3Path(DAG_S3_PATH)
                .pluginsS3Path(PLUGINS_S3_PATH)
                .pluginsS3ObjectVersion(PLUGINS_S3_OBJECT_VERSION)
                .requirementsS3Path(REQUIREMENTS_S3_PATH)
                .requirementsS3ObjectVersion(REQUIREMENTS_S3_OBJECT_VERSION)
                .startupScriptS3Path(STARTUP_SCRIPT_S3_PATH)
                .startupScriptS3ObjectVersion(STARTUP_SCRIPT_S3_OBJECT_VERSION)
                .environmentClass(ENVIRONMENT_CLASS)
                .maxWorkers(MAX_WORKERS)
                .minWorkers(MIN_WORKERS)
                .schedulers(SCHEDULERS)
                .weeklyMaintenanceWindowStart(WEEKLY_MAINTENANCE_WINDOW_START)
                .airflowConfigurationOptions(ImmutableMap.of(KEY, VALUE))
                .networkConfiguration(new NetworkConfiguration(
                        ImmutableList.of(SUBNET_ID_1, SUBNET_ID_2),
                        ImmutableList.of(SECURITY_GROUP_1, SECURITY_GROUP_2)
                ))
                .loggingConfiguration(new LoggingConfiguration(
                        new ModuleLoggingConfiguration(ENABLED, LOG_LEVEL_1, ARN_1),
                        new ModuleLoggingConfiguration(DISABLED, LOG_LEVEL_2, ARN_2),
                        new ModuleLoggingConfiguration(ENABLED, LOG_LEVEL_3, ARN_3),
                        new ModuleLoggingConfiguration(DISABLED, LOG_LEVEL_4, ARN_4),
                        new ModuleLoggingConfiguration(ENABLED, LOG_LEVEL_5, ARN_5)))
                .tags(ImmutableMap.of(KEY, VALUE))
                .webserverAccessMode(PRIVATE_ONLY)
                .endpointManagement(SERVICE)
                .build();
    }

    Environment createApiEnvironment(final EnvironmentStatus status) {
        return Environment.builder()
                .name(NAME)
                .executionRoleArn(EXECUTION_ROLE_ARN)
                .kmsKey(KMS_KEY)
                .airflowVersion(AIRFLOW_VERSION)
                .sourceBucketArn(SOURCE_BUCKET_ARN)
                .dagS3Path(DAG_S3_PATH)
                .pluginsS3Path(PLUGINS_S3_PATH)
                .pluginsS3ObjectVersion(PLUGINS_S3_OBJECT_VERSION)
                .requirementsS3Path(REQUIREMENTS_S3_PATH)
                .requirementsS3ObjectVersion(REQUIREMENTS_S3_OBJECT_VERSION)
                .startupScriptS3Path(STARTUP_SCRIPT_S3_PATH)
                .startupScriptS3ObjectVersion(STARTUP_SCRIPT_S3_OBJECT_VERSION)
                .environmentClass(ENVIRONMENT_CLASS)
                .maxWorkers(MAX_WORKERS)
                .minWorkers(MIN_WORKERS)
                .schedulers(SCHEDULERS)
                .weeklyMaintenanceWindowStart(WEEKLY_MAINTENANCE_WINDOW_START)
                .airflowConfigurationOptions(ImmutableMap.of(KEY, VALUE))
                .networkConfiguration(createNetworkConfiguration())
                .loggingConfiguration(createLoggingConfiguration())
                .tags(ImmutableMap.of(KEY, VALUE, KEY_INTERNAL, VALUE_INTERNAL))
                .webserverAccessMode(WebserverAccessMode.PRIVATE_ONLY)
                .endpointManagement(EndpointManagement.SERVICE)
                .status(status)
                .build();
    }


    static void checkResponseNeedsCallback(final ProgressEvent<ResourceModel, CallbackContext> response) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(CALLBACK_DELAY.getSeconds());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getCallbackContext().isStabilizing()).isTrue();
    }

    static void checkResponseIsSuccess(
            final ProgressEvent<ResourceModel, CallbackContext> response,
            final ResourceModel desiredState) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(desiredState);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getCallbackContext()).isNull();
    }

    private software.amazon.awssdk.services.mwaa.model.NetworkConfiguration createNetworkConfiguration() {
        return software.amazon.awssdk.services.mwaa.model.NetworkConfiguration.builder()
                .subnetIds(ImmutableList.of(SUBNET_ID_1, SUBNET_ID_2))
                .securityGroupIds(ImmutableList.of(SECURITY_GROUP_1, SECURITY_GROUP_2))
                .build();
    }

    private software.amazon.awssdk.services.mwaa.model.LoggingConfiguration createLoggingConfiguration() {
        return software.amazon.awssdk.services.mwaa.model.LoggingConfiguration.builder()
                .dagProcessingLogs(
                        software.amazon.awssdk.services.mwaa.model.ModuleLoggingConfiguration.builder()
                                .enabled(ENABLED)
                                .logLevel(LOG_LEVEL_1)
                                .cloudWatchLogGroupArn(ARN_1)
                                .build())
                .schedulerLogs(
                        software.amazon.awssdk.services.mwaa.model.ModuleLoggingConfiguration.builder()
                                .enabled(DISABLED)
                                .logLevel(LOG_LEVEL_2)
                                .cloudWatchLogGroupArn(ARN_2)
                                .build())
                .webserverLogs(
                        software.amazon.awssdk.services.mwaa.model.ModuleLoggingConfiguration.builder()
                                .enabled(ENABLED)
                                .logLevel(LOG_LEVEL_3)
                                .cloudWatchLogGroupArn(ARN_3)
                                .build())
                .workerLogs(
                        software.amazon.awssdk.services.mwaa.model.ModuleLoggingConfiguration.builder()
                                .enabled(DISABLED)
                                .logLevel(LOG_LEVEL_4)
                                .cloudWatchLogGroupArn(ARN_4)
                                .build())
                .taskLogs(
                        software.amazon.awssdk.services.mwaa.model.ModuleLoggingConfiguration.builder()
                                .enabled(ENABLED)
                                .logLevel(LOG_LEVEL_5)
                                .cloudWatchLogGroupArn(ARN_5)
                                .build())
                .build();
    }
}
