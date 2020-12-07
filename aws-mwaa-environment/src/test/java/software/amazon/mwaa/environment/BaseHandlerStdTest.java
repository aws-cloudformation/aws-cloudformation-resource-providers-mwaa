// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.cloudformation.loggers.LogPublisher;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.mwaa.Proxies;

/**
 * Tests for {@link BaseHandlerStd}.
 */
class BaseHandlerStdTest extends HandlerTestBase {
    /**
     * Prepares mocks.
     */
    @BeforeEach
    public void setup() {
        setupProxies();
    }

    @Test
    public void handleRequestNullContext() {
        // given
        final TestHandler handler = new TestHandler();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .awsAccountId("an-account")
                .region("us-west-2")
                .build();
        // when
        handler.handleRequest(getProxies().getAwsClientProxy(), request, null, null);

        // then
        assertThat(handler.getContext()).isNotNull();
        assertThat(handler.getProxies()).isNotNull();
        assertThat(handler.getProxies().getAwsClientProxy()).isEqualTo(getProxies().getAwsClientProxy());
        assertThat(handler.getProxies().getMwaaClientProxy()).isNotNull();
        assertThat(handler.getProxies().getMwaaClientProxy().client()).isNotNull();
    }

    @Test
    public void handleRequestNotNullContext() {
        // given
        final TestHandler handler = new TestHandler();
        final CallbackContext callbackContext = new CallbackContext();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .awsAccountId("an-account")
                .region("us-west-2")
                .build();

        // when
        handler.handleRequest(getProxies().getAwsClientProxy(), request, callbackContext, null);

        // then
        assertThat(handler.getContext()).isEqualTo(callbackContext);
        assertThat(handler.getProxies()).isNotNull();
        assertThat(handler.getProxies().getAwsClientProxy()).isEqualTo(getProxies().getAwsClientProxy());
        assertThat(handler.getProxies().getMwaaClientProxy()).isNotNull();
        assertThat(handler.getProxies().getMwaaClientProxy().client()).isNotNull();
    }

    @Test
    public void logNonNull() {
        // given
        final TestHandler handler = new TestHandler();
        final TestLogPublisher publisher = new TestLogPublisher();
        final LoggerProxy logger = new LoggerProxy();
        logger.addLogPublisher(publisher);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .awsAccountId("an-account")
                .region("us-west-2")
                .build();

        // when
        handler.handleRequest(getProxies().getAwsClientProxy(), request, null, logger);

        // then
        assertThat(publisher.getPublishedMessages()).isEqualTo("testing string 1");
    }

    /**
     * Testable no-op handler which logs calls for handleRequest.
     */
    private static class TestHandler extends BaseHandlerStd {

        private Proxies proxies;
        private CallbackContext context;

        @Override
        protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
                Proxies requestProxies,
                ResourceHandlerRequest<ResourceModel> request,
                CallbackContext callbackContext) {
            this.proxies = requestProxies;
            this.context = callbackContext;
            log("testing %s %d", "string", 1);
            return null;
        }

        public Proxies getProxies() {
            return proxies;
        }

        public CallbackContext getContext() {
            return context;
        }
    }

    /**
     * Testable log publisher which allows reading back logged messages.
     */
    private static class TestLogPublisher extends LogPublisher {
        private final StringBuilder messages = new StringBuilder();

        @Override
        protected void publishMessage(String message) {
            messages.append(message);
        }

        public String getPublishedMessages() {
            return messages.toString();
        }
    }
}
