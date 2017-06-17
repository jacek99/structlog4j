package com.github.structlog4j.yaml.test;

import java.io.StringReader;
import java.util.List;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import lombok.experimental.UtilityClass;
import org.slf4j.impl.LogEntry;
import org.yaml.snakeyaml.Yaml;

/**
 * Common YAMK test utilities
 */
@UtilityClass // Lombok
public class YamlTestUtils {

    // SnakeYaml object is not threadsafe, need Thread local instance for max performance
    private static final ThreadLocal<Yaml> YAML = new ThreadLocal<Yaml>() {
        @Override
        protected Yaml initialValue() {
            return new Yaml();
        }
    };

    /**
     * Ensures message is valid YAML
     */
    public void assertYamlMessage(List<LogEntry> entries, int entryIndex) {
        try {
            YAML.get().load(entries.get(entryIndex).getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse: " + entries.get(entryIndex).getMessage(),e);
        }
    }

    /**
     * Ensures all messages are valid JSON
     */
    public void assertYamlMessages(List<LogEntry> entries) {
        for(int i = 0; i < entries.size(); i++) {
            assertYamlMessage(entries,i);
        }
    }
}
