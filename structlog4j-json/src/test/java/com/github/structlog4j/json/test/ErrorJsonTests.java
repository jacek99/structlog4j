package com.github.structlog4j.json.test;

import static com.github.structlog4j.test.TestUtils.*;
import static org.assertj.core.api.Assertions.*;

import com.github.structlog4j.IToLog;
import com.github.structlog4j.SLogger;
import com.github.structlog4j.SLoggerFactory;
import com.github.structlog4j.StructLog4J;
import com.github.structlog4j.json.JsonFormatter;
import com.github.structlog4j.test.TestUtils;
import com.github.structlog4j.test.samples.TestSecurityContext;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.impl.LogEntry;
import org.slf4j.impl.TestLogger;

/** Tests for handling of invalid input */
public class ErrorJsonTests {

  private SLogger log;
  private LinkedList<LogEntry> entries;

  @BeforeEach
  public void setup() {
    TestUtils.initForTesting();
    StructLog4J.setFormatter(JsonFormatter.getInstance());

    log = (SLogger) SLoggerFactory.getLogger(ErrorJsonTests.class);
    entries = ((TestLogger) log.getSlfjLogger()).getEntries();
  }

  @Test
  public void testInvalidJson() {
    // ensure our JSON validation logic actually works
    List<LogEntry> entries = new ArrayList<>();
    entries.add(new LogEntry(Level.ERROR, "{corrupted JSON]", Optional.empty()));
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> JsonTestUtils.assertJsonMessage(entries, 0));
  }

  @Test
  public void justKeyButNoValueTest() {
    log.error("This is an error", "just_key_but_no_value");

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    // the key with missing value was not logged
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("{\"message\":\"This is an error\"}");
  }

  @Test
  public void keyWithSpacesTest() {
    log.error("This is an error", "key with spaces", 1L);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("Key with spaces was passed in: key with spaces");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("{\"message\":\"This is an error\"}");
  }

  @Test
  public void keyWithSpacesRecoverTest() {

    Throwable t = new RuntimeException("Important exception");
    TestSecurityContext toLog = new TestSecurityContext("test_user", "TEST_TENANT");

    log.error(
        "This is an error", "key with spaces", 1L, "good_key_that_will_be_skipped", 2L, toLog, t);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("Key with spaces was passed in: key with spaces");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    // the second key was ignored even though it was valid, we simply could not rely on the order
    // any more with corrupted keys
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "{\"message\":\"This is an error\",\"userName\":\"test_user\",\"tenantId\":\"TEST_TENANT\",\"errorMessage\":\"Important exception\"}");
    // validate we did not lose the exception even if it was after the key that had the error
    assertThat(entries.get(1).getError()).describedAs(entries.toString()).isPresent();
  }

  @Test
  public void iToLogWithNullTest() {
    IToLog toLog =
        new IToLog() {
          @Override
          public Object[] toLog() {
            return null;
          }
        };

    log.error("This is an error", toLog);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "Null returned from class com.github.structlog4j.json.test.ErrorJsonTests$1.toLog()");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("{\"message\":\"This is an error\"}");
  }

  @Test
  public void iToLogWithWrongNumberOfParametersTest() {
    IToLog toLog =
        new IToLog() {
          @Override
          public Object[] toLog() {
            // do not return second key
            return new Object[] {"key1", "Value1", "key2"};
          }
        };

    log.error("This is an error", toLog);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "Odd number of parameters (3) returned from class com.github.structlog4j.json.test.ErrorJsonTests$2.toLog()");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("{\"message\":\"This is an error\"}");
  }

  @Test
  public void iToLogWithKeyWithSpacesTest() {
    IToLog toLog =
        new IToLog() {
          @Override
          public Object[] toLog() {
            // do not return second key
            return new Object[] {"key1", "Value1", "key with spaces", "Value 2"};
          }
        };

    log.error("This is an error", toLog);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "Key with spaces was passed in from class com.github.structlog4j.json.test.ErrorJsonTests$3.toLog(): key with spaces");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("{\"message\":\"This is an error\",\"key1\":\"Value1\"}");
  }

  @Test
  public void iToLogWithNullKeyTest() {
    IToLog toLog =
        new IToLog() {
          @Override
          public Object[] toLog() {
            // do not return second key
            return new Object[] {"key1", "Value1", null, "Value 2"};
          }
        };

    log.error("This is an error", toLog);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "Non-String or null key was passed in from class com.github.structlog4j.json.test.ErrorJsonTests$4.toLog(): null (null)");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("{\"message\":\"This is an error\",\"key1\":\"Value1\"}");
  }

  @Test
  public void duplicateMessageKeyTest() {
    log.error("This is a message", "message", "This is another message");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertMessage(
        entries,
        0,
        Level.WARN,
        "Key 'message' renamed to 'message2' in order to avoid overriding default JSON message field. Please correct in your code.",
        false);

    JsonTestUtils.assertJsonMessage(entries, 1);
    assertMessage(
        entries,
        1,
        Level.ERROR,
        "{\"message\":\"This is a message\",\"message2\":\"This is another message\"}",
        false);
  }

  @Test
  public void valueWithQuotes() {
    log.error("This is a message", "key1", "Some \" value with \" quotes");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    JsonTestUtils.assertJsonMessage(entries, 0);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "{\"message\":\"This is a message\",\"key1\":\"Some \\\" value with \\\" quotes\"}",
        false);
  }

  @Test
  public void messageWithQuotes() {
    log.error("This is a \"message\"", "key1", "Some \" value with \" quotes");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    JsonTestUtils.assertJsonMessage(entries, 0);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "{\"message\":\"This is a \\\"message\\\"\",\"key1\":\"Some \\\" value with \\\" quotes\"}",
        false);
  }

  @Test
  public void keyWithQuotes() {
    // you are sick if you try to do this in your code...but let's be sure just in case
    log.error("This is message", "key\"1", "Some value");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    JsonTestUtils.assertJsonMessage(entries, 0);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "{\"message\":\"This is message\",\"key\\\"1\":\"Some value\"}",
        false);
  }
}
