package com.github.structlog4j;

import com.github.structlog4j.format.IFormatter;
import com.github.structlog4j.format.KeyValuePairFormatter;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * Common settings
 *
 * @author Jacek Furmakiewicz
 */
@UtilityClass // Lombok
public class StructLog4J {

    private IFormatter formatter = KeyValuePairFormatter.getInstance();
    private Optional<IToLog> mandatoryContextSupplier = Optional.empty();

    // thread local StringBuilder used for all log concatenation
    final ThreadLocal<StringBuilder> BLD = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder();
        }

        @Override
        public StringBuilder get() {
            super.get().setLength(0);
            return super.get();
        }

        FunctionalInterface t;
    };

    /**
     * Allows to override the default formatter. Should be only done once during application startup
     * from the main thread to avoid any concurrency issues
     * @param formatter Custom formatter implementing the IFormatter interface
     */
    public void setFormatter(IFormatter formatter) {
        StructLog4J.formatter = formatter;
    }

    /**
     * Allows to pas in a lambda that will be invoked on every log entry to add additional mandatory
     * key/value pairs (e.g. hostname, service name, etc). Saves the hassle of having to specify it explicitly
     * on every log invocation
     * @param mandatoryContextSupplier Lambda that will executed on every log entry.
     */
    public void setMandatoryContextSupplier(IToLog mandatoryContextSupplier) {
        StructLog4J.mandatoryContextSupplier = Optional.of(mandatoryContextSupplier);
    }

    // internal
    IFormatter getFormatter() {
        return formatter;
    }

    // internal
    Optional<IToLog> getMandatoryContextSupplier() {
        return mandatoryContextSupplier;
    }

    // internal testing support
    void clearMandatoryContextSupplier() {
        mandatoryContextSupplier = Optional.empty();
    }

}
