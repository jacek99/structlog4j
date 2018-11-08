package com.github.structlog4j.yaml;

import com.github.structlog4j.IFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Basic YAML formatter using the standard SnakeYaml library
 *
 * @author Jacek Furmankiewicz
 */
public class YamlFormatter implements IFormatter<Map<String,String>> {

    // SnakeYaml object is not threadsafe, need Thread local instance for max performance
    private static final ThreadLocal<Yaml> YAML = new ThreadLocal<Yaml>() {
        @Override
        protected Yaml initialValue() {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

            return new Yaml(options);
        }
    };

    private String fieldMessage = "message";
    private static final String FIELD_MESSAGE_2 = "message2";

    private static final YamlFormatter INSTANCE = new YamlFormatter();
    public static YamlFormatter getInstance() {return INSTANCE;}

    public IFormatter<Map<String,String>> setMessageName(String field) {
        fieldMessage = field;
        return this;
    }

    @Override
    public final Map<String,String> start(Logger log) {
        return new HashMap<>(10);
    }

    @Override
    public final IFormatter<Map<String,String>> addMessage(Logger log, Map<String,String> bld, String message) {
        bld.put(fieldMessage,message);
        return this;
    }

    @Override
    public final IFormatter<Map<String,String>> addKeyValue(Logger log, Map<String,String> bld, String key, Object value) {
        // avoid overriding the "message" field
        if (key.equals(fieldMessage)) {
            key = FIELD_MESSAGE_2;
            log.warn("Key '{}' renamed to '{}' in order to avoid overriding default YAML message field. Please correct in your code.",fieldMessage,FIELD_MESSAGE_2);
        }

        bld.put(key, String.valueOf(value));
        return this;
    }

    @Override
    public final String end(Logger log, Map<String,String> bld) {
        return YAML.get().dump(bld).trim();
    }

}
