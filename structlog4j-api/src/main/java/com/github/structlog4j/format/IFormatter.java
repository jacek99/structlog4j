package com.github.structlog4j.format;

/**
 * Standard format/encoder interface
 *
 * @author Jacek Furmankiewicz
 */
public interface IFormatter {

    public IFormatter start(StringBuilder bld);

    public IFormatter addMessage(StringBuilder bld, String message);

    public IFormatter addKey(StringBuilder bld, String key);

    public IFormatter addValue(StringBuilder bld, Object value);

    public IFormatter end(StringBuilder bld);


}
