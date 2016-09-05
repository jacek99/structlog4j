package com.github.structlog4j;

/**
 * Core standard structured logger inteface
 *
 * @author Jacek Furmankiewicz
 */
public interface ILogger {
    public void error(String message, Object...params);
    public void warn(String message, Object...params);
    public void info(String message, Object...params);
    public void debug(String message, Object...params);
    public void trace(String message, Object...params);
}
