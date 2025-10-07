package org.fsg.assetmanager.testutils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

public class CustomMatchers {
    private CustomMatchers() {
        // Hint: Only accepts static methods
    }

    public static Matcher<ILoggingEvent> hasFormattedLog(
            final Level level,
            final String message) {
        return allOf(
                hasLogLevel(level),
                hasFormattedLogMessage(message)
        );
    }

    private static Matcher<ILoggingEvent> hasLogLevel(Level level) {
        return hasProperty("level", equalTo(level));
    }

    private static Matcher<ILoggingEvent> hasFormattedLogMessage(final String message) {
        return hasProperty("formattedMessage", equalTo(message));
    }
}
