package com.github.structlog4j;

import com.github.structlog4j.format.IEncoder;
import com.github.structlog4j.format.KeyValuePairEncoder;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

/**
 * Common settings
 *
 * @author Jacek Furmakiewicz
 */
@UtilityClass
public class StructLog4J {

    private LogFormat LOG_FORMAT = LogFormat.KEY_VALUE_PAIR;
    private IEncoder ENCODER = KeyValuePairEncoder.getInstance();

    // thread local StringBuilder used for all log concatenation
    final ThreadLocal<StringBuilder> BLD= new ThreadLocal<StringBuilder>() {
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
     * Name of environment variable which can be set (e.g. in Docker container)
     * to control what format we use (plain string, JSON, etc)
     */
    public static final String ENV_STRUCTLOG4J_FORMAT = "STRUCTLOG4J_FORMAT";

    static {

    }

    public void setFormat(LogFormat format) {
        if (format == LogFormat.KEY_VALUE_PAIR) {

        } else {

        }
    }
}
