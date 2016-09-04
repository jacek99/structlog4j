package com.github.structlog4j.format;

/**
 * Standard format interface
 *
 * @author Jacek Furmankiewicz
 */
public interface IEncoder {

    /**
     * Returns a string representation of the message + context params,
     * in whichever format the format supports
     * @param message Base error message
     * @param params List of IToLog or key/value pairs to log in a structured way[
     * @return Log entry (plain string, JSON, YAML, etc)
     */
    public String asString(String message, Object...params);
}
