package com.github.structlog4j;

import org.slf4j.Logger;

/** Standard key value pair encoder that formats messages as: message key1=value1 key2=value2 etc */
public class KeyValuePairFormatter implements IFormatter<StringBuilder> {

  private static final String SPACE = " ";
  private static final String QUOTES = "\"";
  private static final String QUOTES_ESCAPED = "\\\"";
  private static final String EQUAL = "=";

  private static final KeyValuePairFormatter INSTANCE = new KeyValuePairFormatter();

  public static KeyValuePairFormatter getInstance() {
    return INSTANCE;
  }

  // thread local StringBuilder used for all log concatenation
  private final transient ThreadLocal<StringBuilder> builderThreadLocal =
      new ThreadLocal<>() {
        @Override
        protected StringBuilder initialValue() {
          return new StringBuilder();
        }

        @Override
        public StringBuilder get() {
          super.get().setLength(0);
          return super.get();
        }
      };

  @Override
  public StringBuilder start(Logger log) {
    return builderThreadLocal.get();
  }

  @Override
  public IFormatter<StringBuilder> addMessage(Logger log, StringBuilder bld, String message) {
    bld.append(message);
    return this;
  }

  @Override
  public IFormatter<StringBuilder> addKeyValue(
      Logger log, StringBuilder bld, String key, Object val) {
    bld.append(SPACE).append(key).append(EQUAL);

    String value = String.valueOf(val);
    value = value.replace(QUOTES, QUOTES_ESCAPED);
    if (value.indexOf(SPACE) < 0) {
      // no spaces in the value, no need to surround it with quotes
      bld.append(value);
    } else {
      bld.append(QUOTES).append(value).append(QUOTES);
    }
    return this;
  }

  @Override
  public String end(Logger log, StringBuilder bld) {
    return bld.toString();
  }
}
