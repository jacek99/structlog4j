package com.github.structlog4j.yaml.test;

import static com.github.structlog4j.test.TestUtils.assertEntries;
import static com.github.structlog4j.test.TestUtils.assertMessage;
import static com.github.structlog4j.yaml.test.YamlTestUtils.*;
import static org.assertj.core.api.Assertions.*;

import com.github.structlog4j.IToLog;
import com.github.structlog4j.SLogger;
import com.github.structlog4j.SLoggerFactory;
import com.github.structlog4j.StructLog4J;
import com.github.structlog4j.test.TestUtils;
import com.github.structlog4j.test.samples.TestSecurityContext;
import com.github.structlog4j.yaml.YamlFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.impl.LogEntry;
import org.slf4j.impl.TestLogger;

/** Tests for handling of invalid input */
@SuppressWarnings({
  "PMD.BeanMembersShouldSerialize",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidDuplicateLiterals"
})
public class ErrorYamlTests {

  private SLogger log;
  private List<LogEntry> entries;

  @BeforeEach
  public void setUp() {
    TestUtils.initForTesting();
    StructLog4J.setFormatter(YamlFormatter.getInstance());

    log = (SLogger) SLoggerFactory.getLogger(ErrorYamlTests.class);
    entries = ((TestLogger) log.getSlfjLogger()).getEntries();
  }

  @Test
  public void testInvalidYaml() {
    // ensure our Yaml validation logic actually works
    List<LogEntry> entries = new ArrayList<>();
    entries.add(new LogEntry(Level.ERROR, "{corrupted JSON]", Optional.empty()));
    assertThatExceptionOfType(RuntimeException.class)
        .describedAs(entries.toString())
        .isThrownBy(() -> assertYamlMessage(entries, 0));
  }

  @Test
  public void justKeyButNoValueTest() {
    log.error("This is an error", "just_key_but_no_value");

    // does not actually generate an error, just shows the value as empty
    // the key with missing value was not logged
    assertEntries(entries, "message: This is an error");
  }

  @Test
  public void keyWithSpacesTest() {
    log.error("This is an error", "key with spaces", 1L);

    assertEntries(
        entries,
        // does not actually generate an error, just shows the value as empty
        "Key with spaces was passed in: key with spaces",
        // validate that despite the error we still managed to process the log entry and logged as
        // much
        // as we could
        "message: This is an error");
  }

  @Test
  public void keyWithSpacesRecoverTest() {

    Throwable t = new RuntimeException("Important exception");
    TestSecurityContext toLog = new TestSecurityContext("test_user", "TEST_TENANT");

    log.error(
        "This is an error", "key with spaces", 1L, "good_key_that_will_be_skipped", 2L, toLog, t);

    assertEntries(
        entries,
        // does not actually generate an error, just shows the value as empty
        "Key with spaces was passed in: key with spaces",
        // validate that despite the error we still managed to process the log entry and logged as
        // much as we could  the second key was ignored even though it was valid, we simply could
        // not rely on the order any more with corrupted keys
        "tenantId: TEST_TENANT\n"
            + "errorMessage: Important exception\n"
            + "message: This is an error\n"
            + "userName: test_user");

    // validate we did not lose the exception even if it was after the key that had the error
    assertThat(entries.get(1).getError()).describedAs(entries.toString()).isPresent();
  }

  @Test
  @SuppressWarnings("PMD.ReturnEmptyArrayRatherThanNull")
  public void iToLogWithNullTest() {
    IToLog toLog =
        new IToLog() {
          @Override
          public Object[] toLog() {
            return null;
          }
        };

    log.error("This is an error", toLog);

    assertEntries(
        entries,
        // does not actually generate an error, just shows the value as empty
        "Null returned from class com.github.structlog4j.yaml.test.ErrorYamlTests$1.toLog()",
        // validate that despite the error we still managed to process the log entry and logged as
        // much
        // as we could
        "message: This is an error");
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

    assertEntries(
        entries,
        // does not actually generate an error, just shows the value as empty
        "Odd number of parameters (3) returned from class com.github.structlog4j.yaml.test.ErrorYamlTests$2.toLog()",
        // validate that despite the error we still managed to process the log entry and logged as
        // much
        // as we could
        "message: This is an error");
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

    assertEntries(
        entries,
        // does not actually generate an error, just shows the value as empty
        "Key with spaces was passed in from class com.github.structlog4j.yaml.test.ErrorYamlTests$3.toLog(): key with spaces",
        // validate that despite the error we still managed to process the log entry and logged as
        // much
        // as we could
        "key1: Value1\n" + "message: This is an error");
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

    assertEntries(
        entries,
        // does not actually generate an error, just shows the value as empty
        "Non-String or null key was passed in from class com.github.structlog4j.yaml.test.ErrorYamlTests$4.toLog(): null (null)",
        // validate that despite the error we still managed to process the log entry and logged as
        // much
        // as we could
        "key1: Value1\n" + "message: This is an error");
  }

  @Test
  public void duplicateMessageKeyTest() {
    log.error("This is a message", "message", "This is another message");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertMessage(
        entries,
        0,
        Level.WARN,
        "Key 'message' renamed to 'message2' in order to avoid overriding default YAML message field. Please correct in your code.",
        false);

    assertYamlMessage(entries, 1);
    assertMessage(
        entries,
        1,
        Level.ERROR,
        "message: This is a message\n" + "message2: This is another message",
        false);
  }

  @Test
  public void valueWithQuotes() {
    log.error("This is a message", "key1", "Some \" value with \" quotes");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "key1: Some \" value with \" quotes\n" + "message: This is a message",
        false);
  }

  @Test
  public void messageWithQuotes() {
    log.error("This is a \"message\"", "key1", "Some \" value with \" quotes");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);

    assertYamlMessage(entries, 0);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "key1: Some \" value with \" quotes\n" + "message: This is a \"message\"",
        false);
  }

  @Test
  public void keyWithQuotes() {
    // you are sick if you try to do this in your code...but let's be sure just in case
    log.error("This is message", "key\"1", "Some value");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);

    assertYamlMessage(entries, 0);
    assertMessage(
        entries, 0, Level.ERROR, "message: This is message\n" + "key\"1: Some value", false);
  }
}
