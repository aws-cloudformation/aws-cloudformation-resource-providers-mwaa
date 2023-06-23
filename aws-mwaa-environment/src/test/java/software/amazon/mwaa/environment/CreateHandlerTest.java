// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.mwaa.MwaaClient;
import software.amazon.awssdk.services.mwaa.model.AccessDeniedException;
import software.amazon.awssdk.services.mwaa.model.CreateEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.CreateEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.Environment;
import software.amazon.awssdk.services.mwaa.model.EnvironmentStatus;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.InternalServerException;
import software.amazon.awssdk.services.mwaa.model.ResourceNotFoundException;
import software.amazon.awssdk.services.mwaa.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.mwaa.translator.CreateTranslator;
import software.amazon.mwaa.translator.ReadTranslator;

/**
 * Tests for {@link CreateHandler}.
 */
@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends HandlerTestBase {
    private static final String INVALID_DATA = "INVALID_DATA";
    private static final int NUMBER_OF_CALLBACKS = 3;

    /**
     * Prepares mocks.
     */
    @BeforeEach
    public void setup() {
        setupProxies();
    }

    /**
     * Makes sure SDK client is called and not overused.
     */
    @AfterEach
    public void tearDown() {
        verify(getSdkClient(), atLeastOnce()).serviceName();
        verifyNoMoreInteractions(getSdkClient());
    }

    /**
     * Tests a happy path.
     */
    @Test
    public void handleRequestSimpleSuccess() {
        // given
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = createCfnModel();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetEnvironmentResponse creating = createGetCreatingEnvironmentResponse();
        final GetEnvironmentResponse available = createGetAvailableEnvironmentResponse();

        when(getSdkClient().getEnvironment(any(GetEnvironmentRequest.class)))
                // at first the environment does not exist
                .thenThrow(ResourceNotFoundException.class)
                // for a while after creation it still doesn't exist
                .thenThrow(ResourceNotFoundException.class)
                // then it is in creating mode
                .thenReturn(creating)
                .thenReturn(creating)
                // then it is created
                .thenReturn(available)
                // final result
                .thenReturn(available);

        final CreateEnvironmentResponse createEnvironmentResponse = CreateEnvironmentResponse.builder().build();
        when(getSdkClient().createEnvironment(any(CreateEnvironmentRequest.class)))
                .thenReturn(createEnvironmentResponse);

        // when
        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                getProxies(), request, new CallbackContext());

        // then
        checkResponseNeedsCallback(response);

        // three times: first when env does not exist immediately after creation, then two times when it is in CREATING
        for (int i = 1; i <= NUMBER_OF_CALLBACKS; i++) {
            // when called back
            response = handler.handleRequest(getProxies(), request, response.getCallbackContext());

            // then
            checkResponseNeedsCallback(response);
        }

        // when called back after environment is created
        response = handler.handleRequest(getProxies(), request, response.getCallbackContext());

        // then
        checkResponseIsSuccess(response, request.getDesiredResourceState());
    }

    /**
     * Tests a sad path.
     */
    @Test
    public void handleRequestFailDuringCreation() {
        // given
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = createCfnModel();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetEnvironmentResponse creating = createGetCreatingEnvironmentResponse();
        final GetEnvironmentResponse failed = createGetFailedEnvironmentResponse();

        when(getSdkClient().getEnvironment(any(GetEnvironmentRequest.class)))
                // at first the environment does not exist
                .thenThrow(ResourceNotFoundException.class)
                // it is in creating mode
                .thenReturn(creating)
                // then it is failed during creation
                .thenReturn(failed);

        final CreateEnvironmentResponse createEnvironmentResponse = CreateEnvironmentResponse.builder().build();
        when(getSdkClient().createEnvironment(any(CreateEnvironmentRequest.class)))
                .thenReturn(createEnvironmentResponse);

        // when
        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                getProxies(), request, new CallbackContext());

        // then
        checkResponseNeedsCallback(response);

        // when
        response = handler.handleRequest(getProxies(), request, response.getCallbackContext());

        // then
        checkResponseNeedsCallback(response);

        // when called back after environment creation is failed
        response = handler.handleRequest(getProxies(), request, response.getCallbackContext());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo("Creation failed");
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
        assertThat(response.getCallbackContext()).isNull();
    }

    /**
     * Asserts throwing {@link CfnAlreadyExistsException} when the environment to create already exist.
     */
    @Test
    public void handleRequestAlreadyExists() {
        // given
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder().name("NAME").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetEnvironmentResponse available = createGetAvailableEnvironmentResponse();

        when(getSdkClient().getEnvironment(any(GetEnvironmentRequest.class))).thenReturn(available);

        // when
        try {
            handler.handleRequest(
                    getProxies(),
                    request,
                    new CallbackContext());
            // then
            fail("Expected CfnAlreadyExistsException");
        } catch (CfnAlreadyExistsException e) {
            // expect exception
            assertThat(e.getMessage().contains(ResourceModel.TYPE_NAME)).isTrue();
        }
    }

    /**
     * Asserts throwing {@link CfnInvalidRequestException} when request experiences transient error on retry.
     */
    @Test
    public void handleRequestInvalidInputNonRetryableException() {
        // given
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder().name("NAME").kmsKey(INVALID_DATA).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final GetEnvironmentRequest awsGetEnvironmentRequest = ReadTranslator.translateToReadRequest(model);
        final CreateEnvironmentRequest awsCreateEnvironmentRequest = CreateTranslator.translateToCreateRequest(model);
        ProxyClient<MwaaClient> mwaaClientProxy = getProxies().getMwaaClientProxy();

        // when
        when(mwaaClientProxy.injectCredentialsAndInvokeV2(awsGetEnvironmentRequest,
                mwaaClientProxy.client()::getEnvironment))
                .thenThrow(ResourceNotFoundException.class);

        when(mwaaClientProxy.injectCredentialsAndInvokeV2(awsCreateEnvironmentRequest,
                mwaaClientProxy.client()::createEnvironment))
                .thenThrow(AccessDeniedException.builder().message(INVALID_DATA).build());

        // then
        assertThatThrownBy(() -> handler.handleRequest(
                getProxies(),
                request,
                new CallbackContext())
        ).isInstanceOf(CfnInvalidRequestException.class);

        verify(getSdkClient(), times(1)).createEnvironment(
                any(CreateEnvironmentRequest.class));
    }

    /**
     * Asserts that environment is successfully created after recovering from both
     * {@link ValidationException} and {@link InternalServerException}.
     * @param mwaaCreateEnvironmentExceptionClass class of Exception to be thrown by
     *                                            mwaa client upon CreateEnvironment request.
     */
    @ParameterizedTest
    @ValueSource(classes = {ValidationException.class, InternalServerException.class})
    public void handleRequestInvalidInputRecovery(Class<? extends Exception> mwaaCreateEnvironmentExceptionClass) {
        // given
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder().name("NAME").kmsKey(INVALID_DATA).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final GetEnvironmentRequest awsGetEnvironmentRequest = ReadTranslator.translateToReadRequest(model);
        final CreateEnvironmentRequest awsCreateEnvironmentRequest = CreateTranslator.translateToCreateRequest(model);
        final CreateEnvironmentResponse createEnvironmentResponse = CreateEnvironmentResponse.builder().build();

        ProxyClient<MwaaClient> mwaaClientProxy = getProxies().getMwaaClientProxy();
        final int expectedNumberOfInvocations = 5;

        // when
        when(mwaaClientProxy.injectCredentialsAndInvokeV2(awsGetEnvironmentRequest,
                mwaaClientProxy.client()::getEnvironment))
                // to indicate this environment does not exists and allow creation
                .thenThrow(ResourceNotFoundException.class);

        when(mwaaClientProxy.injectCredentialsAndInvokeV2(awsCreateEnvironmentRequest,
                mwaaClientProxy.client()::createEnvironment))
                .thenThrow(mwaaCreateEnvironmentExceptionClass)
                .thenThrow(mwaaCreateEnvironmentExceptionClass)
                .thenThrow(mwaaCreateEnvironmentExceptionClass)
                .thenThrow(mwaaCreateEnvironmentExceptionClass)
                .thenReturn(createEnvironmentResponse);
        // then
        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                getProxies(),
                request,
                new CallbackContext());
        checkResponseNeedsCallback(response);
        verify(getSdkClient(), times(expectedNumberOfInvocations)).createEnvironment(
                any(CreateEnvironmentRequest.class));
    }

    /**
     * Asserts throwing {@link CfnInvalidRequestException} when given model has invalid data.
     */
    @Test
    public void handleRequestInvalidInput() {
        // given
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = ResourceModel.builder().name("NAME").kmsKey(INVALID_DATA).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final CreateEnvironmentRequest awsCreateEnvironmentRequest = CreateTranslator.translateToCreateRequest(model);
        final GetEnvironmentRequest awsGetEnvironmentRequest = ReadTranslator.translateToReadRequest(model);

        ProxyClient<MwaaClient> mwaaClientProxy = getProxies().getMwaaClientProxy();

        // when
        when(mwaaClientProxy.injectCredentialsAndInvokeV2(awsGetEnvironmentRequest,
                mwaaClientProxy.client()::getEnvironment))
                // to indicate this environment does not exists and allow creation
                .thenThrow(ResourceNotFoundException.class);

        when(mwaaClientProxy.injectCredentialsAndInvokeV2(awsCreateEnvironmentRequest,
                mwaaClientProxy.client()::createEnvironment))
                .thenThrow(ValidationException.builder().message(INVALID_DATA).build());

        try {
            handler.handleRequest(
                    getProxies(),
                    request,
                    new CallbackContext());
            // then
            fail("Expected CfnInvalidRequestException");
        } catch (CfnInvalidRequestException e) {
            // expect exception
            assertThat(e.getMessage().contains(INVALID_DATA)).isTrue();
            verify(getSdkClient(), times(CreateHandler.MAX_RETRIES)).createEnvironment(
                    any(CreateEnvironmentRequest.class));
        }
    }

    private GetEnvironmentResponse createGetAvailableEnvironmentResponse() {
        final Environment environment = createApiEnvironment(EnvironmentStatus.AVAILABLE);
        return GetEnvironmentResponse.builder().environment(environment).build();
    }

    private GetEnvironmentResponse createGetFailedEnvironmentResponse() {
        final Environment environment = createApiEnvironment(EnvironmentStatus.CREATE_FAILED);
        return GetEnvironmentResponse.builder().environment(environment).build();
    }

    private GetEnvironmentResponse createGetCreatingEnvironmentResponse() {
        final Environment environment = createApiEnvironment(EnvironmentStatus.CREATING);
        return GetEnvironmentResponse.builder().environment(environment).build();
    }

}
