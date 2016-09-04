package com.github.structlog4j;

/**
 * Core standard structured logger inteface
 *
 * @author Jacek Furmankiewicz
 */
public interface ISLogger {

    public void error(String message, Object...params);

}
