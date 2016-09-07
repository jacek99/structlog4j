package org.slf4j.impl;

import lombok.Value;
import org.slf4j.event.Level;

import java.util.Optional;

/**
 * Log entry for testing
 * @author Jacek Furmankiewicz
 */
@Value
public class LogEntry {
    private Level level;
    private String message;
    private Optional<Throwable> error;
}
