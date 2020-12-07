// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.mwaa.model.DeleteEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.DeleteEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.Environment;
import software.amazon.awssdk.services.mwaa.model.EnvironmentStatus;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Tests for {@link DeleteHandler}.
 */
@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends HandlerTestBase {
    private static final int NUMBER_OF_CALLBACKS = 2;

    /**
     * Prepares mocks.
     */
    @BeforeEach
    public void setup() {
        setupProxies();
    }

    /**
     * Tests a happy path.
     */
    @Test
    public void handleRequestSimpleSuccess() {
        // given
        final DeleteHandler handler = new DeleteHandler();
        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetEnvironmentResponse available = createGetAvailableEnvironmentResponse();
        final GetEnvironmentResponse deleting = createGetDeletingEnvironmentResponse();

        when(getSdkClient().getEnvironment(any(GetEnvironmentRequest.class)))
                // at first the environment exists
                .thenReturn(available)
                // then it stays in deleting mode for a while
                .thenReturn(deleting)
                .thenReturn(deleting)
                // at the end it is deleted
                .thenThrow(ResourceNotFoundException.class);

        final DeleteEnvironmentResponse awsDeleteEnvironmentResponse = DeleteEnvironmentResponse.builder().build();
        when(getSdkClient().deleteEnvironment(any(DeleteEnvironmentRequest.class)))
                .thenReturn(awsDeleteEnvironmentResponse);

        // when
        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                getProxies(),
                request,
                new CallbackContext());

        // then
        checkResponseNeedsCallback(response);

        for (int i = 1; i <= NUMBER_OF_CALLBACKS; i++) {
            // when called back
            response = handler.handleRequest(getProxies(), request, response.getCallbackContext());

            // then
            checkResponseNeedsCallback(response);
        }

        // when called back after environment is deleted
        response = handler.handleRequest(getProxies(), request, response.getCallbackContext());

        // then
        checkDeletedEnvironmentResponse(response);

        verify(getSdkClient(), atLeastOnce()).serviceName();
        verifyNoMoreInteractions(getSdkClient());
    }

    /**
     * Asserts throwing {@link CfnNotFoundException} when the environment to delete does not exist.
     */
    @Test
    public void handleRequestNonExistenceEnvironment() {
        // given
        final DeleteHandler handler = new DeleteHandler();
        final ResourceModel model = ResourceModel.builder().name("NAME").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(getSdkClient().getEnvironment(any(GetEnvironmentRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        // when
        try {
            handler.handleRequest(
                    getProxies(),
                    request,
                    new CallbackContext());
            // then
            fail("Expected CfnNotFoundException");
        } catch (CfnNotFoundException e) {
            // expect exception
            assertThat(e.getMessage().contains(ResourceModel.TYPE_NAME));

            verify(getSdkClient(), atLeastOnce()).serviceName();
            verifyNoMoreInteractions(getSdkClient());
        }
    }

    /**
     * Simulates a special case in contract tests when desired model is pass as null and expectation is success.
     */
    @Test
    public void handleRequestNullModel() {
        // given
        final DeleteHandler handler = new DeleteHandler();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(null)
                .build();

        // when
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                getProxies(),
                request,
                new CallbackContext());

        // then
        checkDeletedEnvironmentResponse(response);

        verifyNoMoreInteractions(getSdkClient());
    }

    private GetEnvironmentResponse createGetAvailableEnvironmentResponse() {
        final Environment environment = createApiEnvironment(EnvironmentStatus.AVAILABLE);
        return GetEnvironmentResponse.builder().environment(environment).build();
    }

    private GetEnvironmentResponse createGetDeletingEnvironmentResponse() {
        final Environment environment = createApiEnvironment(EnvironmentStatus.DELETING);
        return GetEnvironmentResponse.builder().environment(environment).build();
    }

    private void checkDeletedEnvironmentResponse(ProgressEvent<ResourceModel, CallbackContext> response) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

}
