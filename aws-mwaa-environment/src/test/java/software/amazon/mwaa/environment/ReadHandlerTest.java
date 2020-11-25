// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.assertj.core.api.Assertions.assertThat;
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
import software.amazon.awssdk.services.mwaa.model.Environment;
import software.amazon.awssdk.services.mwaa.model.EnvironmentStatus;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentRequest;
import software.amazon.awssdk.services.mwaa.model.GetEnvironmentResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Tests for {@link ReadHandler}.
 */
@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends HandlerTestBase {

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
        final ReadHandler handler = new ReadHandler();
        final ResourceModel model = createCfnModel();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final Environment environment = createApiEnvironment(EnvironmentStatus.AVAILABLE);
        final GetEnvironmentResponse awsResponse = GetEnvironmentResponse.builder().environment(environment).build();
        when(getSdkClient().getEnvironment(any(GetEnvironmentRequest.class))).thenReturn(awsResponse);

        // when
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                getProxies(),
                request,
                new CallbackContext());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
