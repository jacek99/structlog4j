package com.github.structlog4j;

import java.util.Optional;
import java.util.function.Function;
import lombok.experimental.UtilityClass;

/**
 * Common settings
 *
 * @author Jacek Furmakiewicz
 */
@UtilityClass // Lombok
public class StructLog4J {

  static final String VALUE_NULL = "null";

  private IFormatter formatter = KeyValuePairFormatter.getInstance();
  private Optional<IToLog> mandatoryContextSupplier = Optional.empty();

  // default formatter just does a toString(), regardless of object type
  private Function<Object, String> defaultValueFormatter =
      (value) -> value == null ? VALUE_NULL : value.toString();

  private Function<Object, String> valueFormatter = defaultValueFormatter;

  /**
   * Allows to override the default formatter. Should be only done once during application startup
   * from the main thread to avoid any concurrency issues
   *
   * @param formatter Custom formatter implementing the IFormatter interface
   */
  public void setFormatter(IFormatter formatter) {
    StructLog4J.formatter = formatter;
  }

  /**
   * Allows to pas in a lambda that will be invoked on every log entry to add additional mandatory
   * key/value pairs (e.g. hostname, service name, etc). Saves the hassle of having to specify it
   * explicitly on every log invocation
   *
   * @param mandatoryContextSupplier Lambda that will executed on every log entry.
   */
  public void setMandatoryContextSupplier(IToLog mandatoryContextSupplier) {
    StructLog4J.mandatoryContextSupplier = Optional.of(mandatoryContextSupplier);
  }

  /**
   * ALlows to define a lambda that can perform custom formatting of any object that is passed in as
   * a value to any key/value entry
   *
   * @param formatter Formatter lambda
   */
  public void setValueFormatter(Function<Object, String> formatter) {
    if (formatter != null) {
      valueFormatter = formatter;
    } else {
      throw new RuntimeException("Value formatter cannot be null");
    }
  }

  /** Gets current log formatter */
  public IFormatter getFormatter() {
    return formatter;
  }

  /** Gets optional mandatory context supplier */
  public Optional<IToLog> getMandatoryContextSupplier() {
    return mandatoryContextSupplier;
  }

  /** Clears the mandatory context supplier (usually for testing purposes only) */
  public void clearMandatoryContextSupplier() {
    mandatoryContextSupplier = Optional.empty();
  }

  /** Returns the value formatter */
  public Function<Object, String> getValueFormatter() {
    return valueFormatter;
  }

  /** Internal usage for formatting purposes */
  public boolean isPrimitiveOrNumber(Class<?> valueType) {
    return valueType.isPrimitive()
        || Boolean.class.equals(valueType)
        || Short.class.equals(valueType)
        || Integer.class.equals(valueType)
        || Long.class.equals(valueType)
        || Double.class.equals(valueType)
        || Float.class.equals(valueType);
  }
}
