package com.github.structlog4j;

import static com.github.structlog4j.test.TestUtils.*;
import static org.assertj.core.api.Assertions.*;

import com.github.structlog4j.test.samples.BusinessObjectContext;
import com.github.structlog4j.test.samples.TestSecurityContext;
import java.util.LinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.impl.LogEntry;
import org.slf4j.impl.TestLogger;

/**
 * Tests for core functionality
 *
 * @author Jacek Furmankiewicz
 */
public class BasicKeyValuePairTests {

  private SLogger log;
  private LinkedList<LogEntry> entries;

  private TestSecurityContext iToLog = new TestSecurityContext("Test User", "TEST_TENANT");

  @BeforeEach
  public void setup() {
    initForTesting();

    log = (SLogger) SLoggerFactory.getLogger(BasicKeyValuePairTests.class);
    entries = ((TestLogger) log.getSlfjLogger()).getEntries();
  }

  @Test
  public void basicTest() {
    log.error("This is an error");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(entries, 0, Level.ERROR, "This is an error", false);
  }

  @Test
  public void singleKeyValueTest() {
    log.error("This is an error", "user", "Jacek");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(entries, 0, Level.ERROR, "This is an error user=Jacek", false);
  }

  @Test
  public void singleKeyValueWithSpaceTest() {
    log.error("This is an error", "user", "Jacek Furmankiewicz");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(entries, 0, Level.ERROR, "This is an error user=\"Jacek Furmankiewicz\"", false);
  }

  @Test
  public void singleKeyNullValueTest() {
    log.error("This is an error", "user", null);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(entries, 0, Level.ERROR, "This is an error user=null", false);
  }

  @Test
  public void multipleKeyValuePairsTest() {
    log.error("This is an error", "user", "John Doe", "tenant", "System", "requestId", "1234");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "This is an error user=\"John Doe\" tenant=System requestId=1234",
        false);
  }

  @Test
  public void iToLogSingleTest() {
    log.error("This is an error", iToLog);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "This is an error userName=\"Test User\" tenantId=TEST_TENANT",
        false);
  }

  @Test
  public void iToLogMultipleTest() {

    BusinessObjectContext ctx = new BusinessObjectContext("Country", "CA");

    log.error("This is an error", iToLog, ctx);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "This is an error userName=\"Test User\" tenantId=TEST_TENANT entityName=Country entityId=CA",
        false);
  }

  @Test
  public void mixedKeyValueIToLogTest() {
    BusinessObjectContext ctx = new BusinessObjectContext("Country", "CA");

    log.error("This is an error", iToLog, ctx, "key1", 1L, "key2", "Value 2");

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "This is an error userName=\"Test User\" tenantId=TEST_TENANT entityName=Country entityId=CA key1=1 key2=\"Value 2\"",
        false);
  }

  @Test
  public void exceptionTest() {

    Throwable t = new RuntimeException("Major exception");

    log.error("This is an error", t);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(
        entries, 0, Level.ERROR, "This is an error errorMessage=\"Major exception\"", true);
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
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "This is an error errorMessage=\"This is the root cause of the error\"",
        true);
  }

  @Test
  public void exceptionWithKeyValueTest() {

    Throwable t = new RuntimeException("Major exception");

    log.error("This is an error", "key1", 1L, "key2", "Value 2", t);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(1);
    assertMessage(
        entries,
        0,
        Level.ERROR,
        "This is an error key1=1 key2=\"Value 2\" errorMessage=\"Major exception\"",
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

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(3);
    for (LogEntry entry : entries) {
      assertThat(entry.getLevel()).describedAs(entries.toString()).isEqualTo(Level.ERROR);
      assertThat(entry.getError()).describedAs(entries.toString()).isPresent();
    }

    // first
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "This is an error userName=\"Test User\" "
                + "tenantId=TEST_TENANT entityName=Country entityId=CA key1=1 key2=\"Value 2\" "
                + "errorMessage=\"This is the root cause of the error\"");
    // second
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "This is an error errorMessage=\"This is the root cause of the error\" "
                + "userName=\"Test User\" tenantId=TEST_TENANT "
                + "entityName=Country entityId=CA key1=1 key2=\"Value 2\"");
    // third
    assertThat(entries.get(2).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "This is an error userName=\"Test User\" tenantId=TEST_TENANT key1=1 "
                + "errorMessage=\"This is the root cause of the error\" "
                + "entityName=Country entityId=CA key2=\"Value 2\"");
  }

  @Test
  public void allLevelsTest() {

    log.error("Error", iToLog);
    log.warn("Warning", iToLog);
    log.info("Information", iToLog);
    log.debug("Debug", iToLog);
    log.trace("Trace", iToLog);

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(5);

    assertThat(entries.get(0).getLevel()).describedAs(entries.toString()).isEqualTo(Level.ERROR);
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("Error userName=\"Test User\" tenantId=TEST_TENANT");

    assertThat(entries.get(1).getLevel()).describedAs(entries.toString()).isEqualTo(Level.WARN);
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("Warning userName=\"Test User\" tenantId=TEST_TENANT");

    assertThat(entries.get(2).getLevel()).describedAs(entries.toString()).isEqualTo(Level.INFO);
    assertThat(entries.get(2).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("Information userName=\"Test User\" tenantId=TEST_TENANT");

    assertThat(entries.get(3).getLevel()).describedAs(entries.toString()).isEqualTo(Level.DEBUG);
    assertThat(entries.get(3).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("Debug userName=\"Test User\" tenantId=TEST_TENANT");

    assertThat(entries.get(4).getLevel()).describedAs(entries.toString()).isEqualTo(Level.TRACE);
    assertThat(entries.get(4).getMessage())
        .describedAs(entries.toString())
        .isEqualTo("Trace userName=\"Test User\" tenantId=TEST_TENANT");
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

    assertThat(entries.size()).describedAs(entries.toString()).isEqualTo(3);
    for (LogEntry entry : entries) {
      assertThat(entry.getLevel()).describedAs(entries.toString()).isEqualTo(Level.ERROR);
      assertThat(entry.getError()).describedAs(entries.toString()).isPresent();
    }

    // all messages should have mandatory context fields specified at the end

    // first
    assertThat(entries.get(0).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "This is an error userName=\"Test User\" tenantId=TEST_TENANT "
                + "entityName=Country entityId=CA key1=1 key2=\"Value 2\" "
                + "errorMessage=\"This is the root cause of the error\" hostname=Titanic serviceName=MyService");
    // second
    assertThat(entries.get(1).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "This is an error errorMessage=\"This is the root cause of the error\" "
                + "userName=\"Test User\" tenantId=TEST_TENANT entityName=Country entityId=CA "
                + "key1=1 key2=\"Value 2\" hostname=Titanic serviceName=MyService");
    // third
    assertThat(entries.get(2).getMessage())
        .describedAs(entries.toString())
        .isEqualTo(
            "This is an error userName=\"Test User\" tenantId=TEST_TENANT key1=1 "
                + "errorMessage=\"This is the root cause of the error\" "
                + "entityName=Country entityId=CA key2=\"Value 2\" "
                + "hostname=Titanic serviceName=MyService");
  }
}
