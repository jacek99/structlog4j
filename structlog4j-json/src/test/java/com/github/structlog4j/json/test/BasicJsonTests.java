package com.github.structlog4j.json.test;

import com.github.structlog4j.SLogger;
import com.github.structlog4j.SLoggerFactory;
import com.github.structlog4j.StructLog4J;
import com.github.structlog4j.json.JsonFormatter;
import com.github.structlog4j.test.samples.BusinessObjectContext;
import com.github.structlog4j.test.samples.TestSecurityContext;
import com.github.structlog4j.test.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.event.Level;
import org.slf4j.impl.LogEntry;
import org.slf4j.impl.TestLogger;

import java.util.LinkedList;

import static com.github.structlog4j.test.TestUtils.*;
import static com.github.structlog4j.json.test.JsonTestUtils.*;
import static org.junit.Assert.*;

/**
 * JSON Formatter tests
 */
public class BasicJsonTests {

    private SLogger log;
    private LinkedList<LogEntry> entries;
    private TestSecurityContext iToLog = new TestSecurityContext("Test User","TEST_TENANT");

    @Before
    public void setup() {
        TestUtils.initForTesting();
        StructLog4J.setFormatter(JsonFormatter.getInstance());

        log = (SLogger) SLoggerFactory.getLogger(BasicJsonTests.class);
        entries = ((TestLogger)log.getSlfjLogger()).getEntries();

    }

    @Test
    public void basicTest() {
        log.error("This is an error");

        assertEquals(entries.toString(),1,entries.size());
        assertJsonMessage(entries,0);
        assertMessage(entries,0, Level.ERROR,"{\"message\":\"This is an error\"}",false);
    }

    @Test
    public void singleKeyValueTest() {
        log.error("This is an error","user","Jacek");

        assertEquals(entries.toString(),1,entries.size());
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"{\"message\":\"This is an error\",\"user\":\"Jacek\"}", false);
    }

    @Test
    public void singleKeyValueWithSpaceTest() {
        log.error("This is an error","user","Jacek Furmankiewicz");

        assertEquals(entries.toString(),1,entries.size());
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries,0, Level.ERROR,"{\"message\":\"This is an error\",\"user\":\"Jacek Furmankiewicz\"}",false);
    }

    @Test
    public void singleKeyNullValueTest() {
        log.error("This is an error","user",null);

        assertEquals(entries.toString(),1,entries.size());
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"{\"message\":\"This is an error\",\"user\":null}",false);
    }

    @Test
    public void multipleKeyValuePairsTest() {
        log.error("This is an error","user","John Doe","tenant","System","requestId","1234");

        assertEquals(entries.toString(),1,entries.size());
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries,0,Level.ERROR, "{\"message\":\"This is an error\",\"user\":\"John Doe\",\"tenant\":\"System\",\"requestId\":\"1234\"}",false);
    }

    @Test
    public void iToLogSingleTest() {
        log.error("This is an error",iToLog);

        assertEquals(entries.toString(),1,entries.size());
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"{\"message\":\"This is an error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\"}", false);
    }


    @Test
    public void iToLogMultipleTest() {

        BusinessObjectContext ctx = new BusinessObjectContext("Country","CA");

        log.error("This is an error",iToLog,ctx);

        assertEquals(entries.toString(),1,entries.size());
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries, 0, Level.ERROR, "{\"message\":\"This is an error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\",\"entityName\":\"Country\",\"entityId\":\"CA\"}",false);
    }

    @Test
    public void mixedKeyValueIToLogTest() {
        BusinessObjectContext ctx = new BusinessObjectContext("Country","CA");

        log.error("This is an error",iToLog,ctx,"key1",1L,"key2","Value 2");

        assertEquals(entries.toString(),1,entries.size());
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,
                "{\"message\":\"This is an error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\",\"entityName\":\"Country\",\"entityId\":\"CA\",\"key1\":1,\"key2\":\"Value 2\"}",
                false);

    }

    @Test
    public void exceptionTest() {

        Throwable t = new RuntimeException("Major exception");

        log.error("This is an error",t);

        assertEquals(entries.toString(),1,entries.size());
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"{\"message\":\"This is an error\",\"errorMessage\":\"Major exception\"}",true);
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
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"{\"message\":\"This is an error\",\"errorMessage\":\"This is the root cause of the error\"}",true);
    }


    @Test
    public void exceptionWithKeyValueTest() {

        Throwable t = new RuntimeException("Major exception");

        log.error("This is an error","key1",1L,"key2","Value 2",t);

        assertEquals(entries.toString(),1,entries.size());
        JsonTestUtils.assertJsonMessage(entries,0);
        assertMessage(entries,0,Level.ERROR,"{\"message\":\"This is an error\",\"key1\":1,\"key2\":\"Value 2\",\"errorMessage\":\"Major exception\"}",
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
        JsonTestUtils.assertJsonMessage(entries,0);
        assertEquals(entries.toString(),"{\"message\":\"This is an error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\",\"entityName\":\"Country\",\"entityId\":\"CA\",\"key1\":1,\"key2\":\"Value 2\",\"errorMessage\":\"This is the root cause of the error\"}",
                entries.get(0).getMessage());
        // second
        JsonTestUtils.assertJsonMessage(entries,1);
        assertEquals(entries.toString(),"{\"message\":\"This is an error\",\"errorMessage\":\"This is the root cause of the error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\",\"entityName\":\"Country\",\"entityId\":\"CA\",\"key1\":1,\"key2\":\"Value 2\"}",
                entries.get(1).getMessage());
        // third
        JsonTestUtils.assertJsonMessage(entries,2);
        assertEquals(entries.toString(),"{\"message\":\"This is an error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\",\"key1\":1,\"errorMessage\":\"This is the root cause of the error\",\"entityName\":\"Country\",\"entityId\":\"CA\",\"key2\":\"Value 2\"}",
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
        JsonTestUtils.assertJsonMessages(entries);

        assertEquals(entries.toString(),Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"{\"message\":\"Error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\"}",entries.get(0).getMessage());

        assertEquals(entries.toString(),Level.WARN,entries.get(1).getLevel());
        assertEquals(entries.toString(),"{\"message\":\"Warning\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\"}",entries.get(1).getMessage());

        assertEquals(entries.toString(),Level.INFO,entries.get(2).getLevel());
        assertEquals(entries.toString(),"{\"message\":\"Information\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\"}",entries.get(2).getMessage());

        assertEquals(entries.toString(),Level.DEBUG,entries.get(3).getLevel());
        assertEquals(entries.toString(),"{\"message\":\"Debug\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\"}",entries.get(3).getMessage());

        assertEquals(entries.toString(),Level.TRACE,entries.get(4).getLevel());
        assertEquals(entries.toString(),"{\"message\":\"Trace\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\"}",entries.get(4).getMessage());

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
        JsonTestUtils.assertJsonMessages(entries);

        for(LogEntry entry : entries) {
            assertEquals(entries.toString(), Level.ERROR,entry.getLevel());
            assertTrue(entries.toString(), entry.getError().isPresent());
        }

        // all messages should have mandatory context fields specified at the end

        // first

        assertEquals(entries.toString(),"{\"message\":\"This is an error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\",\"entityName\":\"Country\",\"entityId\":\"CA\",\"key1\":1,\"key2\":\"Value 2\",\"errorMessage\":\"This is the root cause of the error\",\"hostname\":\"Titanic\",\"serviceName\":\"MyService\"}",
                entries.get(0).getMessage());
        // second
        assertEquals(entries.toString(),"{\"message\":\"This is an error\",\"errorMessage\":\"This is the root cause of the error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\",\"entityName\":\"Country\",\"entityId\":\"CA\",\"key1\":1,\"key2\":\"Value 2\",\"hostname\":\"Titanic\",\"serviceName\":\"MyService\"}",
                entries.get(1).getMessage());
        // third
        assertEquals(entries.toString(),"{\"message\":\"This is an error\",\"userName\":\"Test User\",\"tenantId\":\"TEST_TENANT\",\"key1\":1,\"errorMessage\":\"This is the root cause of the error\",\"entityName\":\"Country\",\"entityId\":\"CA\",\"key2\":\"Value 2\",\"hostname\":\"Titanic\",\"serviceName\":\"MyService\"}",
                entries.get(2).getMessage());
    }

}

