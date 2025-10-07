package org.fsg.assetmanager.testutils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class LogAppender extends AppenderBase<ILoggingEvent> {
    private final Level level;

    @Getter
    private final List<ILoggingEvent> events = new ArrayList<>();

    public LogAppender() {
        this.level = Level.WARN;
    }

    public LogAppender(Level level) {
        this.level = level;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event.getLevel().isGreaterOrEqual(level)) {
            events.add(event);
        }
    }

    public void clear() {
        events.clear();
    }
}
