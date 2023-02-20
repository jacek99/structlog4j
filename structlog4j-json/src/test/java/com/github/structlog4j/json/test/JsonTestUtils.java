package com.github.structlog4j.json.test;

import java.io.StringReader;
import java.util.List;
import javax.json.Json;
import javax.json.JsonReader;
import lombok.experimental.UtilityClass;
import org.slf4j.impl.LogEntry;

/** Common JSON test utilities */
@UtilityClass // Lombok
public class JsonTestUtils {
  /** Ensures message is valid JSON */
  public void assertJsonMessage(List<LogEntry> entries, int entryIndex) {
    try (JsonReader reader =
        Json.createReader(new StringReader(entries.get(entryIndex).getMessage()))) {
      reader.read();
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse: " + entries.get(entryIndex).getMessage(), e);
    }
  }

  /** Ensures all messages are valid JSON */
  public void assertJsonMessages(List<LogEntry> entries) {
    for (int i = 0; i < entries.size(); i++) {
      assertJsonMessage(entries, i);
    }
  }
}
