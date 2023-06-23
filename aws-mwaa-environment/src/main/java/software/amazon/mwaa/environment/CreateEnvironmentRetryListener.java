// Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package software.amazon.mwaa.environment;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import software.amazon.cloudformation.proxy.Logger;

/**
 * Listener for CreateEnvironment retries.
 */
public class CreateEnvironmentRetryListener implements RetryListener {
    private long attemptNumber = 0;
    private long delaySinceFirstAttempt = 0;

    private final Logger logger;
    private final int maxRetries;
    private final String environmentName;

    /**
     *
     * @param logger logger for retry logging.
     * @param maxRetries maximum retry attempts before failure.
     * @param environmentName name of environment to be created.
     */
    public CreateEnvironmentRetryListener(Logger logger, int maxRetries, String environmentName) {
        this.logger = logger;
        this.maxRetries = maxRetries;
        this.environmentName = environmentName;
    }

    @Override
    public <V> void onRetry(Attempt<V> attempt) {
        attemptNumber = attempt.getAttemptNumber();
        delaySinceFirstAttempt = attempt.getDelaySinceFirstAttempt();

        if (attempt.hasResult()) {
            log("CreateEnvironment [%s]: retry attempt %d/%d successful. Total delay since first attempt: %dms",
                    environmentName, attemptNumber, maxRetries, delaySinceFirstAttempt);
        } else {
            log("CreateEnvironment [%s]: retry attempt %d/%d failed with error message: %s. "
                            + "Total delay since first attempt: %dms",
                    environmentName,
                    attemptNumber,
                    maxRetries,
                    attempt.getExceptionCause().getMessage(),
                    delaySinceFirstAttempt);
        }
    }

    private void log(final String format, final Object... args) {
        if (logger != null) {
            logger.log(String.format(format, args));
        }
    }
}
