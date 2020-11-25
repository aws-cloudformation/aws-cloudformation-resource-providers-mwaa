// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.mwaa.model.ListEnvironmentsRequest;
import software.amazon.awssdk.services.mwaa.model.ListEnvironmentsResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Tests for {@link ListHandler}.
 */

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends HandlerTestBase {
    private static final String TOKEN_1 = "TOKEN_1";
    private static final String TOKEN_2 = "TOKEN_2";
    private static final String NAME_1 = "NAME_1";
    private static final String NAME_2 = "NAME_2";

    /**
     * Prepares mocks.
     */
    @BeforeEach
    public void setup() {
        setupProxies();
    }


    /**
     * Makes sure SDK client is not overused.
     */
    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(getSdkClient());
    }

    /**
     * Tests a happy path.
     */
    @Test
    public void handleRequestSimpleSuccess() {
        // given
        final ListHandler handler = new ListHandler();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .nextToken(TOKEN_1)
                .build();

        final ListEnvironmentsResponse awsListEnvironmentsResponse = ListEnvironmentsResponse.builder()
                .nextToken(TOKEN_2)
                .environments(NAME_1, NAME_2)
                .build();

        when(getSdkClient().listEnvironments(any(ListEnvironmentsRequest.class)))
                .thenReturn(awsListEnvironmentsResponse);

        // when
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                getProxies(),
                request,
                new CallbackContext());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels().size()).isEqualTo(2);
        assertThat(response.getResourceModels().get(0).getName()).isEqualTo(NAME_1);
        assertThat(response.getResourceModels().get(1).getName()).isEqualTo(NAME_2);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getNextToken()).isEqualTo(TOKEN_2);
    }
}
