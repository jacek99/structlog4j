package com.github.structlog4j;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Concrete implementation of the ILogger interface
 *
 * @author Jacek Furmankiewicz
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class SLogger implements ILogger {

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
      log(Level.ERROR, message, params);
    }
  }

  @Override
  public void warn(String message, Object... params) {
    if (slfjLogger.isWarnEnabled()) {
      log(Level.WARN, message, params);
    }
  }

  @Override
  public void info(String message, Object... params) {
    if (slfjLogger.isInfoEnabled()) {
      log(Level.INFO, message, params);
    }
  }

  @Override
  public void debug(String message, Object... params) {
    if (slfjLogger.isDebugEnabled()) {
      log(Level.DEBUG, message, params);
    }
  }

  @Override
  public void trace(String message, Object... params) {
    if (slfjLogger.isTraceEnabled()) {
      log(Level.TRACE, message, params);
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

  @SuppressWarnings({
    "PMD.AvoidReassigningParameters",
    "PMD.GuardLogStatement",
    "PMD.AvoidReassigningLoopVariables",
    "PMD.DataflowAnomalyAnalysis"
  })
  private void log(Level level, String message, Object... params) {
    try {
      // just in case...
      if (message == null) {
        message = "";
      }

      Throwable e = null;
      IFormatter<Object> formatter = StructLog4J.getFormatter();
      Object bld = formatter.start(slfjLogger);
      formatter.addMessage(slfjLogger, bld, message);

      boolean processKeyValues =
          true; // set to false in case we encounter errors and cannot rely on the order any more

      for (int i = 0; i < params.length; i++) {

        Object param = params[i];

        if (param instanceof IToLog) {
          handleIToLog(formatter, bld, (IToLog) param);
        } else if (param instanceof Throwable) {
          // exceptions are not logged directly (unless they implement IToLog)
          // they will get passed separate as exceptions to the base SLF4J API
          e = (Throwable) param;

          // also log the error explicitly as a separate key/value pair for easy parsing
          formatter.addKeyValue(slfjLogger, bld, KEY_ERROR_MESSAGE, getCauseErrorMessage(e));

        } else {
          // dynamic key/value pairs being passed in
          // we only process the key/value pairs if no errors were encountered and we can rely
          // on the order being correct
          if (processKeyValues) {

            // move on to the next field automatically and assume it's the value
            i++;
            if (i < params.length) {
              if (!handleKeyValue(formatter, bld, param, params[i], null)) {
                // error encountered in the key, stop processing other key/value pairs
                processKeyValues = false;
              }
            }
          }
        }
      }

      // add mandatory context, if specified
      Optional<IToLog> mandatory = StructLog4J.getMandatoryContextSupplier();
      mandatory.ifPresent(iToLog -> handleIToLog(formatter, bld, iToLog));

      String logEntry = formatter.end(slfjLogger, bld);

      // actual logging via SLF4J
      log(level, logEntry, e);

    } catch (Exception ex) {
      /// should never happen, a logging library has no right to generate exceptions :-)
      slfjLogger.error("UNEXPECTED LOGGER ERROR: " + ex.getMessage(), ex);
    }
  }

  // handle IToLog implementations
  private void handleIToLog(IFormatter encoder, Object bld, IToLog loggable) {
    Object[] logParams = loggable.toLog();
    // sanity checks
    if (logParams == null) {
      slfjLogger.error("Null returned from {}.toLog()", loggable.getClass());
      return;
    } else if (logParams.length % 2 != 0) {
      slfjLogger.error(
          "Odd number of parameters ({}) returned from {}.toLog()",
          logParams.length,
          loggable.getClass());
      return;
    }

    for (int i = 0; i < logParams.length; i = i + 2) {
      handleKeyValue(encoder, bld, logParams[i], logParams[i + 1], loggable);
    }
  }

  // common logic for handling keys
  // returns true/false depending on whether it was successful or not
  private boolean handleKeyValue(
      IFormatter formatter, Object bld, Object keyObject, Object value, IToLog source) {
    // key must be a String
    if (keyObject != null && keyObject instanceof String) {

      String key = (String) keyObject;
      if (key.indexOf(SPACE) < 0) {
        formatter.addKeyValue(slfjLogger, bld, key, value);
      } else {
        if (source == null) {
          slfjLogger.error("Key with spaces was passed in: {}", key);
        } else {
          slfjLogger.error(
              "Key with spaces was passed in from {}.toLog(): {}", source.getClass(), key);
        }
        return false;
      }

    } else {

      // a non-String key was passed
      if (source == null) {
        slfjLogger.error(
            "Non-String or null key was passed in: {} ({})",
            keyObject,
            keyObject != null ? keyObject.getClass() : "null");
      } else {
        slfjLogger.error(
            "Non-String or null key was passed in from {}.toLog(): {} ({})",
            source.getClass(),
            keyObject,
            keyObject != null ? keyObject.getClass() : "null");
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
          slfjLogger.error(structuredMessage, err);
        }
        break;
      case WARN:
        if (err == null) {
          slfjLogger.warn(structuredMessage);
        } else {
          slfjLogger.warn(structuredMessage, err);
        }
        break;
      case INFO:
        if (err == null) {
          slfjLogger.info(structuredMessage);
        } else {
          slfjLogger.info(structuredMessage, err);
        }
        break;
      case DEBUG:
        if (err == null) {
          slfjLogger.debug(structuredMessage);
        } else {
          slfjLogger.debug(structuredMessage, err);
        }
        break;
      case TRACE:
        if (err == null) {
          slfjLogger.trace(structuredMessage);
        } else {
          slfjLogger.trace(structuredMessage, err);
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
