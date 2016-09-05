package com.github.structlog4j.format;

/**
 * Standard key value pair encoder that formats messages as:
 * message key1=value1 key2=value2 etc
 */
public class KeyValuePairFormatter implements IFormatter {

    private static final String SPACE = " ";
    private static final String QUOTES= "\"";
    private static final String QUOTES_ESCAPED = "\\\"";
    private static final String EQUAL="=";

    private static final KeyValuePairFormatter INSTANCE = new KeyValuePairFormatter();
    public static KeyValuePairFormatter getInstance() {return INSTANCE;}

    @Override
    public IFormatter start(StringBuilder bld) {
        return this;
    }

    @Override
    public IFormatter addMessage(StringBuilder bld, String message) {
        bld.append(message);
        return this;
    }

    @Override
    public IFormatter addKey(StringBuilder bld, String key) {
        bld.append(SPACE).append(key).append(EQUAL);
        return this;
    }

    @Override
    public IFormatter addValue(StringBuilder bld, Object value) {
        String val = String.valueOf(value);
        val = val.replace(QUOTES,QUOTES_ESCAPED);
        if (val.indexOf(SPACE) < 0) {
            // no spaces in the value, no need to surround it with quotes
            bld.append(val);
        } else {
            bld.append(QUOTES).append(val).append(QUOTES);
        }
        return this;
    }

    @Override
    public IFormatter end(StringBuilder bld) {
        return this;
    }
}
