package com.github.structlog4j;

import static org.junit.Assert.*;

import com.github.structlog4j.samples.BusinessObjectContext;
import com.github.structlog4j.samples.TestSecurityContext;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.event.Level;
import org.slf4j.impl.LogEntry;
import org.slf4j.impl.TestLogger;

import java.util.LinkedList;

/**
 * Tests for core functionality
 * @author Jacek Furmankiewicz
 */
public class BasicKeyValuePairTests {

    private SLogger log;
    private LinkedList<LogEntry> entries;

    private TestSecurityContext iToLog = new TestSecurityContext("Test User","TEST_TENANT");

    @Before
    public void setup() {
        log = (SLogger) SLoggerFactory.getLogger(BasicKeyValuePairTests.class);
        entries = ((TestLogger)log.getSlfjLogger()).getEntries();
    }

    @Test
    public void basicTest() {
        log.error("This is an error");

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error",entries.get(0).getMessage());
        assertFalse(entries.toString(),entries.get(0).getError().isPresent());
    }

    @Test
    public void singleKeyValueTest() {
        log.error("This is an error","user","Jacek");

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error user=Jacek",entries.get(0).getMessage());
        assertFalse(entries.toString(),entries.get(0).getError().isPresent());
    }

    @Test
    public void singleKeyValueWithSpaceTest() {
        log.error("This is an error","user","Jacek Furmankiewicz");

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error user=\"Jacek Furmankiewicz\"",entries.get(0).getMessage());
        assertFalse(entries.toString(),entries.get(0).getError().isPresent());
    }

    @Test
    public void singleKeyNullValueTest() {
        log.error("This is an error","user",null);

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error user=null",entries.get(0).getMessage());
        assertFalse(entries.toString(),entries.get(0).getError().isPresent());
    }

    @Test
    public void multipleKeyValuePairsTest() {
        log.error("This is an error","user","John Doe","tenant","System","requestId","1234");

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error user=\"John Doe\" tenant=System requestId=1234",entries.get(0).getMessage());
        assertFalse(entries.toString(),entries.get(0).getError().isPresent());
    }

    @Test
    public void iToLogSingleTest() {
        log.error("This is an error",iToLog);

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error userName=\"Test User\" tenantId=TEST_TENANT",entries.get(0).getMessage());
        assertFalse(entries.toString(),entries.get(0).getError().isPresent());
    }


    @Test
    public void iToLogMultipleTest() {

        BusinessObjectContext ctx = new BusinessObjectContext("Country","CA");

        log.error("This is an error",iToLog,ctx);

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error userName=\"Test User\" tenantId=TEST_TENANT entityName=Country entityId=CA",entries.get(0).getMessage());
        assertFalse(entries.toString(),entries.get(0).getError().isPresent());
    }

    @Test
    public void mixedKeyValueIToLogTest() {
        BusinessObjectContext ctx = new BusinessObjectContext("Country","CA");

        log.error("This is an error",iToLog,ctx,"key1",1L,"key2","Value 2");

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error userName=\"Test User\" tenantId=TEST_TENANT entityName=Country entityId=CA key1=1 key2=\"Value 2\"",entries.get(0).getMessage());
        assertFalse(entries.toString(),entries.get(0).getError().isPresent());
    }

    @Test
    public void exceptionTest() {

        Throwable t = new RuntimeException("Major exception");

        log.error("This is an error",t);

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error errorMessage=\"Major exception\"",entries.get(0).getMessage());
        assertTrue(entries.toString(),entries.get(0).getError().isPresent());
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
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error errorMessage=\"This is the root cause of the error\"",entries.get(0).getMessage());
        assertTrue(entries.toString(),entries.get(0).getError().isPresent());
    }


    @Test
    public void exceptionWithKeyValueTest() {

        Throwable t = new RuntimeException("Major exception");

        log.error("This is an error","key1",1L,"key2","Value 2",t);

        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(), Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"This is an error key1=1 key2=\"Value 2\" errorMessage=\"Major exception\"",entries.get(0).getMessage());
        assertTrue(entries.toString(),entries.get(0).getError().isPresent());
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
        assertEquals(entries.toString(),"This is an error userName=\"Test User\" tenantId=TEST_TENANT entityName=Country entityId=CA key1=1 key2=\"Value 2\" errorMessage=\"This is the root cause of the error\"",
                entries.get(0).getMessage());
        // second
        assertEquals(entries.toString(),"This is an error errorMessage=\"This is the root cause of the error\" userName=\"Test User\" tenantId=TEST_TENANT entityName=Country entityId=CA key1=1 key2=\"Value 2\"",
                entries.get(1).getMessage());
        // third
        assertEquals(entries.toString(),"This is an error userName=\"Test User\" tenantId=TEST_TENANT key1=1 errorMessage=\"This is the root cause of the error\" entityName=Country entityId=CA key2=\"Value 2\"",
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

        assertEquals(entries.toString(),Level.ERROR,entries.get(0).getLevel());
        assertEquals(entries.toString(),"Error userName=\"Test User\" tenantId=TEST_TENANT",entries.get(0).getMessage());

        assertEquals(entries.toString(),Level.WARN,entries.get(1).getLevel());
        assertEquals(entries.toString(),"Warning userName=\"Test User\" tenantId=TEST_TENANT",entries.get(1).getMessage());

        assertEquals(entries.toString(),Level.INFO,entries.get(2).getLevel());
        assertEquals(entries.toString(),"Information userName=\"Test User\" tenantId=TEST_TENANT",entries.get(2).getMessage());

        assertEquals(entries.toString(),Level.DEBUG,entries.get(3).getLevel());
        assertEquals(entries.toString(),"Debug userName=\"Test User\" tenantId=TEST_TENANT",entries.get(3).getMessage());

        assertEquals(entries.toString(),Level.TRACE,entries.get(4).getLevel());
        assertEquals(entries.toString(),"Trace userName=\"Test User\" tenantId=TEST_TENANT",entries.get(4).getMessage());

    }
}
