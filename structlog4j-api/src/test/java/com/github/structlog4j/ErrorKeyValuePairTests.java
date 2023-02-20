package com.github.structlog4j;

import static org.assertj.core.api.Assertions.*;

import com.github.structlog4j.test.TestUtils;
import com.github.structlog4j.test.samples.TestSecurityContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.LogEntry;
import org.slf4j.impl.TestLogger;

/** Tests for handling of invalid input */
@SuppressWarnings({
  "PMD.BeanMembersShouldSerialize",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.DataflowAnomalyAnalysis"
})
public class ErrorKeyValuePairTests {

  public static final String THIS_IS_AN_ERROR = "This is an error";
  private SLogger log;
  private List<LogEntry> entries;

  @BeforeEach
  public void setUp() {
    TestUtils.initForTesting();

    log = (SLogger) SLoggerFactory.getLogger(BasicKeyValuePairTests.class);
    entries = ((TestLogger) log.getSlfjLogger()).getEntries();
  }

  @Test
  public void justKeyButNoValueTest() {
    log.error(THIS_IS_AN_ERROR, "just_key_but_no_value");

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    // the key with missing value was not logged
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(THIS_IS_AN_ERROR);
  }

  @Test
  public void keyWithSpacesTest() {
    log.error(THIS_IS_AN_ERROR, "key with spaces", 1L);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("Key with spaces was passed in: key with spaces");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(THIS_IS_AN_ERROR);
  }

  @Test
  public void keyWithSpacesRecoverTest() {

    Throwable t = new RuntimeException("Important exception");
    TestSecurityContext toLog = new TestSecurityContext("test_user", "TEST_TENANT");

    log.error(
        THIS_IS_AN_ERROR, "key with spaces", 1L, "good_key_that_will_be_skipped", 2L, toLog, t);

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
            "This is an error userName=test_user tenantId=TEST_TENANT errorMessage=\"Important exception\"");
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

    log.error(THIS_IS_AN_ERROR, toLog);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "Null returned from class com.github.structlog4j.ErrorKeyValuePairTests$1.toLog()");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(THIS_IS_AN_ERROR);
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

    log.error(THIS_IS_AN_ERROR, toLog);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "Odd number of parameters (3) returned from class com.github.structlog4j.ErrorKeyValuePairTests$2.toLog()");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(THIS_IS_AN_ERROR);
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

    log.error(THIS_IS_AN_ERROR, toLog);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "Key with spaces was passed in from class com.github.structlog4j.ErrorKeyValuePairTests$3.toLog(): key with spaces");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("This is an error key1=Value1");
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

    log.error(THIS_IS_AN_ERROR, toLog);

    // does not actually generate an error, just shows the value as empty
    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(2);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "Non-String or null key was passed in from class com.github.structlog4j.ErrorKeyValuePairTests$4.toLog(): null (null)");

    // validate that despite the error we still managed to process the log entry and logged as much
    // as we could
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("This is an error key1=Value1");
  }
}
