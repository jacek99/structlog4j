package com.github.structlog4j.yaml.test;

import static com.github.structlog4j.test.TestUtils.*;
import static com.github.structlog4j.yaml.test.YamlTestUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.slf4j.event.Level.ERROR;

import com.github.structlog4j.SLogger;
import com.github.structlog4j.SLoggerFactory;
import com.github.structlog4j.StructLog4J;
import com.github.structlog4j.test.TestUtils;
import com.github.structlog4j.test.samples.BusinessObjectContext;
import com.github.structlog4j.test.samples.TestSecurityContext;
import com.github.structlog4j.yaml.YamlFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.impl.LogEntry;
import org.slf4j.impl.TestLogger;

/** JSON Formatter tests */
@SuppressWarnings({
  "PMD.BeanMembersShouldSerialize",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidDuplicateLiterals"
})
public class BasicYamlTests {

  private SLogger log;
  private List<LogEntry> entries;
  private TestSecurityContext iToLog = new TestSecurityContext("Test User", "TEST_TENANT");

  @BeforeEach
  public void setUp() {
    TestUtils.initForTesting();
    StructLog4J.setFormatter(YamlFormatter.getInstance());

    log = (SLogger) SLoggerFactory.getLogger(BasicYamlTests.class);
    entries = ((TestLogger) log.getSlfjLogger()).getEntries();
  }

  @Test
  public void basicTest() {
    log.error("This is an error");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(entries, 0, ERROR, "message: This is an error", false);
  }

  @Test
  public void singleKeyValueTest() {
    log.error("This is an error", "user", "Jacek");

    assertYamlMessage(entries, 0);
    assertMessage(entries, 0, ERROR, "message: This is an error\n" + "user: Jacek", false);
  }

  @Test
  public void singleKeyValueWithSpaceTest() {
    log.error("This is an error", "user", "Jacek Furmankiewicz");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(
        entries, 0, ERROR, "message: This is an error\n" + "user: Jacek Furmankiewicz", false);
  }

  @Test
  public void singleKeyNullValueTest() {
    log.error("This is an error", "user", null);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(entries, 0, ERROR, "message: This is an error\n" + "user: 'null'", false);
  }

  @Test
  public void multipleKeyValuePairsTest() {
    log.error("This is an error", "user", "John Doe", "tenant", "System", "requestId", "1234");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(
        entries,
        0,
        ERROR,
        "requestId: '1234'\n"
            + "message: This is an error\n"
            + "user: John Doe\n"
            + "tenant: System",
        false);
  }

  @Test
  public void iToLogSingleTest() {
    log.error("This is an error", iToLog);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(
        entries,
        0,
        ERROR,
        "tenantId: TEST_TENANT\n" + "message: This is an error\n" + "userName: Test User",
        false);
  }

  @Test
  public void iToLogMultipleTest() {

    BusinessObjectContext ctx = new BusinessObjectContext("Country", "CA");

    log.error("This is an error", iToLog, ctx);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(
        entries,
        0,
        ERROR,
        "entityName: Country\n"
            + "tenantId: TEST_TENANT\n"
            + "entityId: CA\n"
            + "message: This is an error\n"
            + "userName: Test User",
        false);
  }

  @Test
  public void mixedKeyValueIToLogTest() {
    BusinessObjectContext ctx = new BusinessObjectContext("Country", "CA");

    log.error("This is an error", iToLog, ctx, "key1", 1L, "key2", "Value 2");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(
        entries,
        0,
        ERROR,
        "key1: '1'\n"
            + "key2: Value 2\n"
            + "entityName: Country\n"
            + "tenantId: TEST_TENANT\n"
            + "entityId: CA\n"
            + "message: This is an error\n"
            + "userName: Test User",
        false);
  }

  @Test
  public void exceptionTest() {

    Throwable t = new RuntimeException("Major exception");

    log.error("This is an error", t);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(
        entries, 0, ERROR, "errorMessage: Major exception\n" + "message: This is an error", true);
  }

  /**
   * Ensures the root cause of the exception gets logged as the default message, not the final
   * re-thrown exception
   */
  @Test
  public void exceptionRootCauseTest() {

    Throwable rootCause = new RuntimeException("This is the root cause of the error");
    Throwable t = new RuntimeException("Major exception", rootCause);

    log.error("This is an error", t);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(
        entries,
        0,
        ERROR,
        "errorMessage: This is the root cause of the error\n" + "message: This is an error",
        true);
  }

  @Test
  public void exceptionWithKeyValueTest() {

    Throwable t = new RuntimeException("Major exception");

    log.error("This is an error", "key1", 1L, "key2", "Value 2", t);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertYamlMessage(entries, 0);
    assertMessage(
        entries,
        0,
        ERROR,
        "key1: '1'\n"
            + "key2: Value 2\n"
            + "errorMessage: Major exception\n"
            + "message: This is an error",
        true);
  }

  @Test
  public void kitchenSinkTest() {

    BusinessObjectContext ctx = new BusinessObjectContext("Country", "CA");
    Throwable rootCause = new RuntimeException("This is the root cause of the error");
    Throwable t = new RuntimeException("Major exception", rootCause);

    // mix and match in different order to ensure it all works
    log.error("This is an error", iToLog, ctx, "key1", 1L, "key2", "Value 2", t);
    log.error("This is an error", t, iToLog, ctx, "key1", 1L, "key2", "Value 2");
    log.error("This is an error", iToLog, "key1", 1L, t, ctx, "key2", "Value 2");

    assertEntryLevels(entries, ERROR, ERROR, ERROR);

    // first
    assertYamlMessage(entries, 0);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "key1: '1'\n"
                + "key2: Value 2\n"
                + "entityName: Country\n"
                + "tenantId: TEST_TENANT\n"
                + "errorMessage: This is the root cause of the error\n"
                + "entityId: CA\n"
                + "message: This is an error\n"
                + "userName: Test User");
    // second
    assertYamlMessage(entries, 1);
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "key1: '1'\n"
                + "key2: Value 2\n"
                + "entityName: Country\n"
                + "errorMessage: This is the root cause of the error\n"
                + "tenantId: TEST_TENANT\n"
                + "entityId: CA\n"
                + "message: This is an error\n"
                + "userName: Test User");
    // third
    assertYamlMessage(entries, 2);
    assertThat(entries.get(2).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "key1: '1'\n"
                + "key2: Value 2\n"
                + "entityName: Country\n"
                + "tenantId: TEST_TENANT\n"
                + "errorMessage: This is the root cause of the error\n"
                + "entityId: CA\n"
                + "message: This is an error\n"
                + "userName: Test User");
  }

  @Test
  public void allLevelsTest() {

    log.error("Error", iToLog);
    log.warn("Warning", iToLog);
    log.info("Information", iToLog);
    log.debug("Debug", iToLog);
    log.trace("Trace", iToLog);

    assertEntriesSize(entries, 5);
    assertYamlMessages(entries);

    // verify levels
    assertEntryLevels(entries, ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE);
    assertEntries(
        entries,
        "tenantId: TEST_TENANT\n" + "message: Error\n" + "userName: Test User",
        "tenantId: TEST_TENANT\n" + "message: Warning\n" + "userName: Test User",
        "tenantId: TEST_TENANT\n" + "message: Information\n" + "userName: Test User",
        "tenantId: TEST_TENANT\n" + "message: Debug\n" + "userName: Test User",
        "tenantId: TEST_TENANT\n" + "message: Trace\n" + "userName: Test User");
  }

  @Test
  public void kitchenSinkWithMandatoryContextTest() {

    BusinessObjectContext ctx = new BusinessObjectContext("Country", "CA");
    Throwable rootCause = new RuntimeException("This is the root cause of the error");
    Throwable t = new RuntimeException("Major exception", rootCause);

    // define mandatory context lambfa
    StructLog4J.setMandatoryContextSupplier(
        () -> new Object[] {"hostname", "Titanic", "serviceName", "MyService"});

    // mix and match in different order to ensure it all works
    log.error("This is an error", iToLog, ctx, "key1", 1L, "key2", "Value 2", t);
    log.error("This is an error", t, iToLog, ctx, "key1", 1L, "key2", "Value 2");
    log.error("This is an error", iToLog, "key1", 1L, t, ctx, "key2", "Value 2");

    assertYamlMessages(entries);

    assertEntryLevels(entries, ERROR, ERROR, ERROR);
    assertEntryErrors(entries);

    // all messages should have mandatory context fields specified at the end
    assertEntries(
        entries,
        "key1: '1'\n"
            + "key2: Value 2\n"
            + "hostname: Titanic\n"
            + "entityName: Country\n"
            + "tenantId: TEST_TENANT\n"
            + "errorMessage: This is the root cause of the error\n"
            + "entityId: CA\n"
            + "message: This is an error\n"
            + "userName: Test User\n"
            + "serviceName: MyService",
        "key1: '1'\n"
            + "key2: Value 2\n"
            + "hostname: Titanic\n"
            + "entityName: Country\n"
            + "errorMessage: This is the root cause of the error\n"
            + "tenantId: TEST_TENANT\n"
            + "entityId: CA\n"
            + "message: This is an error\n"
            + "userName: Test User\n"
            + "serviceName: MyService",
        "key1: '1'\n"
            + "key2: Value 2\n"
            + "hostname: Titanic\n"
            + "entityName: Country\n"
            + "tenantId: TEST_TENANT\n"
            + "errorMessage: This is the root cause of the error\n"
            + "entityId: CA\n"
            + "message: This is an error\n"
            + "userName: Test User\n"
            + "serviceName: MyService");
  }
}
