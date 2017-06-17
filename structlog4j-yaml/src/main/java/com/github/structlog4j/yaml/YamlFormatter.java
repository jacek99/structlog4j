package com.github.structlog4j.yaml;

import com.github.structlog4j.IFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

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

    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_MESSAGE_2 = "message2";

    private static final YamlFormatter INSTANCE = new YamlFormatter();
    public static YamlFormatter getInstance() {return INSTANCE;}

    @Override
    public final Map<String,String> start(Logger log) {
        return new HashMap<>(10);
    }

    @Override
    public final IFormatter<Map<String,String>> addMessage(Logger log, Map<String,String> bld, String message) {
        bld.put(FIELD_MESSAGE,message);
        return this;
    }

    @Override
    public final IFormatter<Map<String,String>> addKeyValue(Logger log, Map<String,String> bld, String key, Object value) {
        // avoid overriding the "message" field
        if (key.equals(FIELD_MESSAGE)) {
            key = FIELD_MESSAGE_2;
            log.warn("Key 'message' renamed to 'message2' in order to avoid overriding default YAML message field. Please correct in your code.");
        }

        bld.put(key, String.valueOf(value));
        return this;
    }

    @Override
    public final String end(Logger log, Map<String,String> bld) {
        return YAML.get().dump(bld).trim();
    }

}
