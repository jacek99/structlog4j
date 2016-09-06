package com.github.structlog4j;

import com.github.structlog4j.format.IFormatter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Optional;

/**
 * Concrete implementation of the ILogger interface
 *
 * @author Jacek Furmankiewicz
 */
@RequiredArgsConstructor
class SLogger implements ILogger {

    private static final String KEY_ERROR_MESSAGE = "errorMessage";
    private static final String SPACE = " ";

    @Getter // for testing
    private final org.slf4j.Logger slfjLogger;

    SLogger(String name) {
        slfjLogger = LoggerFactory.getLogger(name);

    }

    SLogger(Class<?> source) {
        slfjLogger = LoggerFactory.getLogger(source);
    }

    @Override
    public void error(String message, Object... params) {
        if (slfjLogger.isErrorEnabled()) {
          log(Level.ERROR,message,params);
        }
    }

    @Override
    public void warn(String message, Object... params) {
        if (slfjLogger.isWarnEnabled()) {
            log(Level.WARN,message,params);
        }
    }

    @Override
    public void info(String message, Object... params) {
        if (slfjLogger.isInfoEnabled()) {
            log(Level.INFO,message,params);
        }
    }

    @Override
    public void debug(String message, Object... params) {
        if (slfjLogger.isDebugEnabled()) {
            log(Level.DEBUG,message,params);
        }
    }

    @Override
    public void trace(String message, Object... params) {
        if (slfjLogger.isTraceEnabled()) {
            log(Level.TRACE,message,params);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return slfjLogger.isErrorEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return slfjLogger.isWarnEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return slfjLogger.isInfoEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return slfjLogger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return slfjLogger.isTraceEnabled();
    }

    private void log(Level level, String message, Object...params) {
        try {
            // just in case...
            if (message == null) {
                message = "";
            }

            Throwable e = null;
            StringBuilder bld = StructLog4J.BLD.get();
            IFormatter formatter = StructLog4J.getFormatter();

            formatter.start(bld).addMessage(bld,message);

            boolean isPreviousKey = false;
            boolean processKeyValues = true; // set to false in case we encounter errors and cannot rely on the order any more

            for (Object param : params) {
                if (param instanceof IToLog) {
                    handleIToLog(formatter,bld, (IToLog) param);
                    isPreviousKey = false;
                } else if (param instanceof Throwable) {
                    // exceptions are not logged directly (unless they implement IToLog)
                    // they will get passed separate as exceptions to the base SLF4J API
                    e = (Throwable) param;

                    // also log the error explicitly as a separate key/value pair for easy parsing
                    formatter.addKey(bld,KEY_ERROR_MESSAGE).addValue(bld,getCauseErrorMessage(e));

                    isPreviousKey = false;
                } else {

                    // we only process the key/value pairs if no errors were encountered and we can rely
                    // on the order being correct
                    if (processKeyValues) {

                        // dynamic key/value pairs being passed in
                        if (isPreviousKey) {
                            formatter.addValue(bld,param);
                            isPreviousKey = false;
                        } else {
                            // key must be a String
                            if (handleKey(formatter,bld,param,null)) {
                                isPreviousKey = true;
                            } else {
                                // error encountered in the key, stop processing other key/value pairs
                                processKeyValues = false;
                            }
                        }
                    }
                }
            }

            // add mandatory context, if specified
            Optional<IToLog> mandatory = StructLog4J.getMandatoryContextSupplier();
            if (mandatory.isPresent()) {
                handleIToLog(formatter,bld,mandatory.get());
            }

            formatter.end(bld);

            // actual logging via SLF4J
            log(level, bld.toString(),e);

        } catch (Exception ex) {
            /// should never happen, a logging library has no right to generate exceptions :-)
            slfjLogger.error("UNEXPECTED LOGGER ERROR",ex);
        }
    }

    // handle IToLog implementations
    private void handleIToLog(IFormatter encoder, StringBuilder bld, IToLog loggable) {
        Object[] logParams = loggable.toLog();
        //sanity checks
        if (logParams == null) {
            slfjLogger.error("Null returned from {}.toLog()",loggable.getClass());
            return;
        } else if (logParams.length % 2 != 0) {
            slfjLogger.error("Odd number of parameters ({}) returned from {}.toLog()",logParams.length,loggable.getClass());
            return;
        }

        for (int i = 0; i < logParams.length; i = i+2) {
            if (handleKey(encoder, bld, logParams[i], loggable)) {
                // key OK, so we can add value
                encoder.addValue(bld, logParams[i + 1]);
            }
        }
    }

    // common logic for handling keys
    // returns true/false depending on whether it was successful or not
    private boolean handleKey(IFormatter formatter, StringBuilder bld, Object param, IToLog source) {
        // key must be a String
        if (param != null && param instanceof String) {

            String key = (String)param;
            if (key.indexOf(SPACE) < 0) {
                formatter.addKey(bld, (String) param);
            } else {
                if (source == null) {
                    slfjLogger.error("Key with spaces was passed in: {}", key);
                } else {
                    slfjLogger.error("Key with spaces was passed in from {}.toLog(): {}", source.getClass(), key);
                }
                return false;
            }

        } else {

            // a non-String key was passed
            if (source == null) {
                slfjLogger.error("Non-String or null key was passed in: {} ({})", param,
                        param != null ? param.getClass() : "null");
            } else {
                slfjLogger.error("Non-String or null key was passed in from {}.toLog(): {} ({})", source.getClass(), param,
                        param != null ? param.getClass() : "null");
            }
            return false;
        }

        // all good
        return true;
    }

    private void log(Level level, String structuredMessage, Throwable err) {
        switch (level) {
            case ERROR:
                if (err == null) {
                    slfjLogger.error(structuredMessage);
                } else {
                    slfjLogger.error(structuredMessage,err);
                }
                break;
            case WARN:
                if (err == null) {
                    slfjLogger.warn(structuredMessage);
                } else {
                    slfjLogger.warn(structuredMessage,err);
                }
                break;
            case INFO:
                if (err == null) {
                    slfjLogger.info(structuredMessage);
                } else {
                    slfjLogger.info(structuredMessage,err);
                }
                break;
            case DEBUG:
                if (err == null) {
                    slfjLogger.debug(structuredMessage);
                } else {
                    slfjLogger.debug(structuredMessage,err);
                }
                break;
            case TRACE:
                if (err == null) {
                    slfjLogger.trace(structuredMessage);
                } else {
                    slfjLogger.trace(structuredMessage,err);
                }
                break;
            default:
                    // do nothing
        }
    }

    // Goes down the exception hierarchy to find the actual error message at the root of the entire
    // stack trace
    private String getCauseErrorMessage(Throwable t) {
        if (t.getCause() == null) {
            return t.getMessage();
        } else {
            return getCauseErrorMessage(t.getCause());
        }
    }
}
