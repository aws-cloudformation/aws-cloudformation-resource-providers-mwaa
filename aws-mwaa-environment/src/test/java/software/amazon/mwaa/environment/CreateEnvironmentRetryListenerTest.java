// Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.rholder.retry.Attempt;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.mwaa.model.CreateEnvironmentResponse;
import software.amazon.awssdk.services.mwaa.model.ValidationException;
import software.amazon.cloudformation.proxy.Logger;

/**
 * Tests for {@link CreateEnvironmentRetryListener}.
 */
public class CreateEnvironmentRetryListenerTest {

    private static final String SUCCESS_KEY_STRING = "successful";
    private static final String FAILURE_KEY_STRING = "failed";
    /**
     * Test for non null logger on retry success.
     */
    @Test
    public void logNonNullRetrySuccess() {
        // given
        final int maxRetries = 8;
        final String environmentName = "ENVIRONMENT_NAME";

        final Logger logger = mock(Logger.class);
        final CreateEnvironmentRetryListener listener = new CreateEnvironmentRetryListener(
                logger, maxRetries, environmentName);
        Attempt<CreateEnvironmentResponse> attempt = mock(Attempt.class);
        // when
        when(attempt.hasResult()).thenReturn(true);
        // then
        listener.onRetry(attempt);
        verify(logger, times(1)).log(Mockito.argThat(s -> s.contains(SUCCESS_KEY_STRING)));
    }

    /**
     * Test for non null logger on retry attempt failure.
     */
    @Test
    public void logNonNullRetryFailure() {
        // given
        final int maxRetries = 8;
        final String environmentName = "ENVIRONMENT_NAME";

        final Logger logger = mock(Logger.class);
        final CreateEnvironmentRetryListener listener = new CreateEnvironmentRetryListener(
                logger, maxRetries, environmentName);
        Attempt<CreateEnvironmentResponse> attempt = mock(Attempt.class);
        // when
        when(attempt.hasResult()).thenReturn(false);
        when(attempt.getExceptionCause()).thenReturn(ValidationException.builder().build());
        // then
        listener.onRetry(attempt);
        verify(logger, times(1)).log(Mockito.argThat(s -> s.contains(FAILURE_KEY_STRING)));
    }
}
