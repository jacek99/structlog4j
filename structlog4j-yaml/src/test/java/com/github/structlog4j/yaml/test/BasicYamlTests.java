package com.github.structlog4j.yaml.test;

import com.github.structlog4j.SLogger;
import com.github.structlog4j.SLoggerFactory;
import com.github.structlog4j.StructLog4J;
import com.github.structlog4j.test.TestUtils;
import com.github.structlog4j.test.samples.BusinessObjectContext;
import com.github.structlog4j.test.samples.TestSecurityContext;
import com.github.structlog4j.yaml.YamlFormatter;
import java.util.LinkedList;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.event.Level;
import org.slf4j.impl.LogEntry;
import org.slf4j.impl.TestLogger;

import static com.github.structlog4j.yaml.test.YamlTestUtils.*;
import static com.github.structlog4j.test.TestUtils.assertMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JSON Formatter tests
 */
public class BasicYamlTests {

    private SLogger log;
    private LinkedList<LogEntry> entries;
    private TestSecurityContext iToLog = new TestSecurityContext("Test User","TEST_TENANT");

    @Before
    public void setup() {
        TestUtils.initForTesting();
        StructLog4J.setFormatter(YamlFormatter.getInstance());

        log = (SLogger) SLoggerFactory.getLogger(BasicYamlTests.class);
        entries = ((TestLogger)log.getSlfjLogger()).getEntries();

    }

    @Test
    public void basicTest() {
        log.error("This is an error");

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0, Level.ERROR,"message: This is an error",false);
    }

    @Test
    public void singleKeyValueTest() {
        log.error("This is an error","user","Jacek");

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"message: This is an error\n" +
                "user: Jacek", false);
    }

    @Test
    public void singleKeyValueWithSpaceTest() {
        log.error("This is an error","user","Jacek Furmankiewicz");

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0, Level.ERROR,"message: This is an error\n" +
                "user: Jacek Furmankiewicz",false);
    }

    @Test
    public void singleKeyNullValueTest() {
        log.error("This is an error","user",null);

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"message: This is an error\n" +
                "user: 'null'",false);
    }

    @Test
    public void multipleKeyValuePairsTest() {
        log.error("This is an error","user","John Doe","tenant","System","requestId","1234");

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0,Level.ERROR, "requestId: '1234'\n" +
                "message: This is an error\n" +
                "user: John Doe\n" +
                "tenant: System",false);
    }

    @Test
    public void iToLogSingleTest() {
        log.error("This is an error",iToLog);

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"tenantId: TEST_TENANT\n" +
                "message: This is an error\n" +
                "userName: Test User", false);
    }


    @Test
    public void iToLogMultipleTest() {

        BusinessObjectContext ctx = new BusinessObjectContext("Country","CA");

        log.error("This is an error",iToLog,ctx);

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries, 0, Level.ERROR, "entityName: Country\n" +
                "tenantId: TEST_TENANT\n" +
                "entityId: CA\n" +
                "message: This is an error\n" +
                "userName: Test User",false);
    }

    @Test
    public void mixedKeyValueIToLogTest() {
        BusinessObjectContext ctx = new BusinessObjectContext("Country","CA");

        log.error("This is an error",iToLog,ctx,"key1",1L,"key2","Value 2");

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,
                "key1: '1'\n" +
                        "key2: Value 2\n" +
                        "entityName: Country\n" +
                        "tenantId: TEST_TENANT\n" +
                        "entityId: CA\n" +
                        "message: This is an error\n" +
                        "userName: Test User",
                false);

    }

    @Test
    public void exceptionTest() {

        Throwable t = new RuntimeException("Major exception");

        log.error("This is an error",t);

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"errorMessage: Major exception\n" +
                "message: This is an error",true);
    }

    /**
     * Ensures the root cause of the exception gets logged as the default message, not the final re-thrown exception
     */
    @Test
    public void exceptionRootCauseTest() {

        Throwable rootCause = new RuntimeException("This is the root cause of the error");
        Throwable t = new RuntimeException("Major exception",rootCause);

        log.error("This is an error",t);

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"errorMessage: This is the root cause of the error\n" +
                "message: This is an error",true);
    }


    @Test
    public void exceptionWithKeyValueTest() {

        Throwable t = new RuntimeException("Major exception");

        log.error("This is an error","key1",1L,"key2","Value 2",t);

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"key1: '1'\n" +
                        "key2: Value 2\n" +
                        "errorMessage: Major exception\n" +
                        "message: This is an error",
                true);
    }

    @Test
    public void kitchenSinkTest() {

        BusinessObjectContext ctx = new BusinessObjectContext("Country","CA");
        Throwable rootCause = new RuntimeException("This is the root cause of the error");
        Throwable t = new RuntimeException("Major exception",rootCause);

        // mix and match in different order to ensure it all works
        log.error("This is an error",iToLog,ctx,"key1",1L,"key2","Value 2",t);
        log.error("This is an error",t,iToLog,ctx,"key1",1L,"key2","Value 2");
        log.error("This is an error",iToLog,"key1",1L,t,ctx,"key2","Value 2");

        assertEquals(entries.toString(),3,entries.size());
        for(LogEntry entry : entries) {
            assertEquals(entries.toString(), Level.ERROR,entry.getLevel());
            assertTrue(entries.toString(), entry.getError().isPresent());
        }

        // first
        assertYamlMessage(entries,0);
        assertEquals(entries.toString(),"key1: '1'\n" +
                        "key2: Value 2\n" +
                        "entityName: Country\n" +
                        "tenantId: TEST_TENANT\n" +
                        "errorMessage: This is the root cause of the error\n" +
                        "entityId: CA\n" +
                        "message: This is an error\n" +
                        "userName: Test User",
                entries.get(0).getMessage());
        // second
        assertYamlMessage(entries,1);
        assertEquals(entries.toString(),"key1: '1'\n" +
                        "key2: Value 2\n" +
                        "entityName: Country\n" +
                        "errorMessage: This is the root cause of the error\n" +
                        "tenantId: TEST_TENANT\n" +
                        "entityId: CA\n" +
                        "message: This is an error\n" +
                        "userName: Test User",
                entries.get(1).getMessage());
        // third
        assertYamlMessage(entries,2);
        assertEquals(entries.toString(),"key1: '1'\n" +
                        "key2: Value 2\n" +
                        "entityName: Country\n" +
                        "tenantId: TEST_TENANT\n" +
                        "errorMessage: This is the root cause of the error\n" +
                        "entityId: CA\n" +
                        "message: This is an error\n" +
                        "userName: Test User",
                entries.get(2).getMessage());
    }

    @Test
    public void allLevelsTest() {

        log.error("Error",iToLog);
        log.warn("Warning",iToLog);
        log.info("Information",iToLog);
        log.debug("Debug",iToLog);
        log.trace("Trace",iToLog);

        assertEquals(entries.toString(),5,entries.size());
        assertYamlMessages(entries);

        assertEquals(entries.toString(),Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"tenantId: TEST_TENANT\n" +
                "message: Error\n" +
                "userName: Test User",entries.get(0).getMessage());

        assertEquals(entries.toString(),Level.WARN,entries.get(1).getLevel());
        assertEquals(entries.toString(),"tenantId: TEST_TENANT\n" +
                "message: Warning\n" +
                "userName: Test User",entries.get(1).getMessage());

        assertEquals(entries.toString(),Level.INFO,entries.get(2).getLevel());
        assertEquals(entries.toString(),"tenantId: TEST_TENANT\n" +
                "message: Information\n" +
                "userName: Test User",entries.get(2).getMessage());

        assertEquals(entries.toString(),Level.DEBUG,entries.get(3).getLevel());
        assertEquals(entries.toString(),"tenantId: TEST_TENANT\n" +
                "message: Debug\n" +
                "userName: Test User",entries.get(3).getMessage());

        assertEquals(entries.toString(),Level.TRACE,entries.get(4).getLevel());
        assertEquals(entries.toString(),"tenantId: TEST_TENANT\n" +
                "message: Trace\n" +
                "userName: Test User",entries.get(4).getMessage());

    }

    @Test
    public void kitchenSinkWithMandatoryContextTest() {

        BusinessObjectContext ctx = new BusinessObjectContext("Country","CA");
        Throwable rootCause = new RuntimeException("This is the root cause of the error");
        Throwable t = new RuntimeException("Major exception",rootCause);

        // define mandatory context lambfa
        StructLog4J.setMandatoryContextSupplier(() -> new Object[]{"hostname","Titanic","serviceName","MyService"});

        // mix and match in different order to ensure it all works
        log.error("This is an error",iToLog,ctx,"key1",1L,"key2","Value 2",t);
        log.error("This is an error",t,iToLog,ctx,"key1",1L,"key2","Value 2");
        log.error("This is an error",iToLog,"key1",1L,t,ctx,"key2","Value 2");

        assertEquals(entries.toString(),3,entries.size());
        assertYamlMessages(entries);

        for(LogEntry entry : entries) {
            assertEquals(entries.toString(), Level.ERROR,entry.getLevel());
            assertTrue(entries.toString(), entry.getError().isPresent());
        }

        // all messages should have mandatory context fields specified at the end

        // first

        assertEquals(entries.toString(),"key1: '1'\n" +
                        "key2: Value 2\n" +
                        "hostname: Titanic\n" +
                        "entityName: Country\n" +
                        "tenantId: TEST_TENANT\n" +
                        "errorMessage: This is the root cause of the error\n" +
                        "entityId: CA\n" +
                        "message: This is an error\n" +
                        "userName: Test User\n" +
                        "serviceName: MyService",
                entries.get(0).getMessage());
        // second
        assertEquals(entries.toString(),"key1: '1'\n" +
                        "key2: Value 2\n" +
                        "hostname: Titanic\n" +
                        "entityName: Country\n" +
                        "errorMessage: This is the root cause of the error\n" +
                        "tenantId: TEST_TENANT\n" +
                        "entityId: CA\n" +
                        "message: This is an error\n" +
                        "userName: Test User\n" +
                        "serviceName: MyService",
                entries.get(1).getMessage());
        // third
        assertEquals(entries.toString(),"key1: '1'\n" +
                        "key2: Value 2\n" +
                        "hostname: Titanic\n" +
                        "entityName: Country\n" +
                        "tenantId: TEST_TENANT\n" +
                        "errorMessage: This is the root cause of the error\n" +
                        "entityId: CA\n" +
                        "message: This is an error\n" +
                        "userName: Test User\n" +
                        "serviceName: MyService",
                entries.get(2).getMessage());
    }

}

