package com.github.structlog4j.test;

import static org.assertj.core.api.Assertions.*;

import com.github.structlog4j.KeyValuePairFormatter;
import com.github.structlog4j.StructLog4J;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.event.Level;
import org.slf4j.impl.LogEntry;

/** Common testing helper methods */
public final class TestUtils {

  /** Resets the config to default settings for every test */
  public static void initForTesting() {
    StructLog4J.clearMandatoryContextSupplier();
    StructLog4J.setFormatter(KeyValuePairFormatter.getInstance());
  }

  /** Asserts a single message */
  public static void assertMessage(
      List<LogEntry> entries,
      int entryIndex,
      Level expectedLevel,
      String expectedMessage,
      boolean expectedExceptionPresent) {
    assertThat(entries.get(entryIndex).getLevel())
        .describedAs(entries.toString())
        .isEqualTo(expectedLevel);

    assertThat(entries.get(entryIndex).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(expectedMessage);

    assertThat(entries.get(entryIndex).getError().isPresent())
        .describedAs(entries.toString())
        .isEqualTo(expectedExceptionPresent);
  }

  /**
   * Asserts a collection of log entries to ensure they are the expected size and contain the
   * expected messages.
   *
   * @param entries List of entries
   * @param expectedMessages Collection of expected messages in each entry, in order
   */
  public static void assertEntries(LinkedList<LogEntry> entries, String... expectedMessages) {
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(expectedMessages.length);

    for (int i = 0; i < expectedMessages.length; i++) {
      assertThat(entries.get(i).getMessage())
          .describedAs(String.valueOf(i) + ": " + entries.toString())
          .isEqualTo(expectedMessages[i]);
    }
  }

  /**
   * Validates entries have expected logging level.
   *
   * @param entries Entries
   * @param expectedLevels Expected Level for every entry, in order
   */
  public static void assertEntryLevels(LinkedList<LogEntry> entries, Level... expectedLevels) {
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(expectedLevels.length);

    for (int i = 0; i < expectedLevels.length; i++) {
      assertThat(entries.get(i).getLevel())
          .describedAs(String.valueOf(i) + ": " + entries.toString())
          .isEqualTo(expectedLevels[i]);
    }
  }

  /**
   * Validates errors are present in every entry
   *
   * @param entries Entries
   */
  public static void assertEntryErrors(LinkedList<LogEntry> entries) {
    entries.forEach(
        e -> {
          assertThat(e.getError()).describedAs(entries.toString()).isPresent();
        });
  }

  /** Asserts number of logged entries. */
  public static void assertEntriesSize(LinkedList<LogEntry> entries, int size) {
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(size);
  }
}
