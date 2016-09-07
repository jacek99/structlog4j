package org.slf4j.impl;

import org.slf4j.ILoggerFactory;

/**
 * Actual implementation of the Slf4J logging API to be used in our unit tests...
 * @author Jacek Furmankiewicz
 */
public class StaticLoggerBinder {

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static final StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    public static String REQUESTED_API_VERSION = "1.7";  // !final

    public ILoggerFactory getLoggerFactory() {
        return new TestLoggerFactory();
    }

    public String getLoggerFactoryClassStr() {
        return TestLoggerFactory.class.getName();
    }
}
