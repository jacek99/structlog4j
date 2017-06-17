package com.github.structlog4j.test;

import static org.junit.Assert.*;

import com.github.structlog4j.KeyValuePairFormatter;
import com.github.structlog4j.StructLog4J;
import lombok.experimental.UtilityClass;
import org.slf4j.event.Level;
import org.slf4j.impl.LogEntry;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;

/**
 * Common testing helper methods
 */
@UtilityClass
public class TestUtils {

    /**
     * Resets the config to default settings for every test
     */
    public void initForTesting() {
        StructLog4J.clearMandatoryContextSupplier();
        StructLog4J.setFormatter(KeyValuePairFormatter.getInstance());
    }

    public void assertMessage(List<LogEntry> entries, int entryIndex, Level expectedLevel, String expectedMessage, boolean expectedExceptionPresent) {
        assertEquals(entries.toString(), expectedLevel, entries.get(entryIndex).getLevel());
        assertEquals(entries.toString(),expectedMessage,entries.get(entryIndex).getMessage());
        assertTrue(entries.toString(),entries.get(entryIndex).getError().isPresent() == expectedExceptionPresent);
    }


}
