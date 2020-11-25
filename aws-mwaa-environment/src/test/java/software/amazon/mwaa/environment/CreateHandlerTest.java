// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.mwaa.model.CreateEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.CreateEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.Environment;
import software.amazon.awssdk.services.mwaa.model.EnvironmentStatus;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.ResourceNotFoundException;
import software.amazon.awssdk.services.mwaa.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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

        when(getSdkClient().getEnvironment(any(GetEnvironmentRequest.class)))
                // to indicate this environment does not exists and allow creation
                .thenThrow(ResourceNotFoundException.class);

        when(getSdkClient().createEnvironment(any(CreateEnvironmentRequest.class)))
                .thenThrow(ValidationException.builder().message(INVALID_DATA).build());

        // when
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
