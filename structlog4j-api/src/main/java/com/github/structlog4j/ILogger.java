package com.github.structlog4j;

/**
 * Core standard structured logger inteface
 *
 * @author Jacek Furmankiewicz
 */
public interface ILogger {

    //logging APIs
    public void error(String message, Object...params);
    public void warn(String message, Object...params);
    public void info(String message, Object...params);
    public void debug(String message, Object...params);
    public void trace(String message, Object...params);

    // logging level checks, usually never needed but we add them for completion
    public boolean isErrorEnabled();
    public boolean isWarnEnabled();
    public boolean isInfoEnabled();
    public boolean isDebugEnabled();
    public boolean isTraceEnabled();
}
