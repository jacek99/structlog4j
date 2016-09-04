package com.github.structlog4j.format;

/**
 * Standard key value pair encoder that formats messages as:
 * message key1=value1 key2=value2 etc
 */
public class KeyValuePairEncoder implements IEncoder {

    private static final KeyValuePairEncoder INSTANCE = new KeyValuePairEncoder();

    public static KeyValuePairEncoder getInstance() {return INSTANCE;}

    @Override
    public String asString(String message, Object... params) {
        return "";
    }
}
