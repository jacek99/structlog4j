package com.github.structlog4j;

import org.slf4j.Logger;

/**
 * Standard format/encoder interface. The SLF4J logger is passed into every method for any internal
 * error reporting within the formatter itself
 *
 * <p>BLD = builder object specific to a formatter passed around from start() till end()
 *
 * @author Jacek Furmankiewicz
 */
public interface IFormatter<BLD> {

  public BLD start(Logger log);

  public IFormatter<BLD> addMessage(Logger log, BLD bld, String message);

  public IFormatter<BLD> addKeyValue(Logger log, BLD bld, String key, Object value);

  /** Returns the formatted log message */
  public String end(Logger log, BLD bld);
}
