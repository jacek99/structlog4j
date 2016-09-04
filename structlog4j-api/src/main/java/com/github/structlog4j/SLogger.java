package com.github.structlog4j;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Concrete implementation of the ISLogger interface
 *
 * @author Jacek Furmankiewicz
 */
@RequiredArgsConstructor
class SLogger implements ISLogger {
    private final Logger LOG;

    SLogger(String name) {
        LOG = LoggerFactory.getLogger(name);

    }

    SLogger(Class<?> source) {
        LOG = LoggerFactory.getLogger(source);
    }

    @Override
    public void error(String message, Object... params) {
        if (LOG.isErrorEnabled()) {
          log(Level.ERROR,message,params);
        }
    }

    private void log(Level level, String message, Object...params) {
        try {
            Throwable e = null;
            StringBuilder bld = StructLog4J.BLD.get();

            for (Object param : params) {
                if (param instanceof IToLog) {
                    // object implements custom logging

                } else if (param instanceof Throwable) {
                    e = (Throwable) param;
                } else {

                }
            }

            // actual logging via SLF4J
            log(level, bld.toString(),e);

        } catch (Exception ex) {
            /// should never happen, a logging library has no right to generate exceptions :-)
            LOG.error("UNEXPECTED LOGGER ERROR",ex);
        }
    }

    private void log(Level level, String structuredMessage, Throwable err) {
        switch (level) {
            case ERROR:
                if (err == null) {
                    LOG.error(structuredMessage);
                } else {
                    LOG.error(structuredMessage,err);
                }
            case WARN:
                if (err == null) {
                    LOG.warn(structuredMessage);
                } else {
                    LOG.warn(structuredMessage,err);
                }
            case INFO:
                if (err == null) {
                    LOG.info(structuredMessage);
                } else {
                    LOG.info(structuredMessage,err);
                }
            case DEBUG:
                if (err == null) {
                    LOG.debug(structuredMessage);
                } else {
                    LOG.debug(structuredMessage,err);
                }
            case TRACE:
                if (err == null) {
                    LOG.trace(structuredMessage);
                } else {
                    LOG.trace(structuredMessage,err);
                }
            default:
                    // do nothing
        }
    }

}
