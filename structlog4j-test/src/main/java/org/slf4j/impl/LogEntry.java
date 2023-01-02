package org.slf4j.impl;

import java.util.Optional;
import lombok.Value;
import org.slf4j.event.Level;

/**
 * Log entry for testing
 *
 * @author Jacek Furmankiewicz
 */
@Value
public class LogEntry {
  private Level level;
  private String message;
  private Optional<Throwable> error;
}
