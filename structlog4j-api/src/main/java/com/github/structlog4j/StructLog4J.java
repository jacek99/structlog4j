package com.github.structlog4j;

import com.github.structlog4j.format.IFormatter;
import com.github.structlog4j.format.KeyValuePairFormatter;
import lombok.experimental.UtilityClass;

/**
 * Common settings
 *
 * @author Jacek Furmakiewicz
 */
@UtilityClass // Lombok
public class StructLog4J {

    private IFormatter formatter = KeyValuePairFormatter.getInstance();

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
    };

    /**
     * Allows to override the default formatter. Should be only done once during application startup
     * from the main thread to avoid any concurrency issues
     * @param formatter Custom formatter implementing the IFormatter interface
     */
    public void setFormatter(IFormatter formatter) {
        StructLog4J.formatter = formatter;
    }

    // Returns the current encoder - internal only
    IFormatter getFormatter() {
        return formatter;
    }
}
