package com.github.structlog4j;

import lombok.experimental.UtilityClass;

/**
 * Structured logger factory
 * @author Jacek Furmankiewicz
 */
@UtilityClass
public class SLoggerFactory {

    /**
     * Returns logger for explicit name
     */
    public ILogger getLogger(String name) {
        return new SLogger(name);
    }

    /**
     * Returns logger for source class that is generating entries
     */
    public ILogger getLogger(Class<?> source) {
        return new SLogger(source);
    }
}
