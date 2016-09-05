package com.github.structlog4j;

import java.util.Map;

/**
 * Interface that can be added to any POJO in order to enable structured logging in it
 * automatically whenever it is passed to the logger
 *
 * @author Jacek Furmankiewicz
 */
public interface IToLog {

    /**
     * Return an array of key/value pairs with values for logging, e.g.
     * return Object[]{"key1",this.getProperty1(),"key2",this.getProperty2()"};
     * @return Array of key/value pairs to log
     */
    public Object[] toLog();
}
