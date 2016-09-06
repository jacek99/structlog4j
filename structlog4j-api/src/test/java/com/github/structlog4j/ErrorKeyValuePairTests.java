package com.github.structlog4j;

import static org.junit.Assert.*;

import com.github.structlog4j.samples.TestSecurityContext;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.impl.LogEntry;
import org.slf4j.impl.TestLogger;

import java.util.LinkedList;

/**
 * Tests for handling of invalid input
 */
public class ErrorKeyValuePairTests {

    private SLogger log;
    private LinkedList<LogEntry> entries;

    @Before
    public void setup() {
        StructLog4J.clearMandatoryContextSupplier();

        log = (SLogger) SLoggerFactory.getLogger(BasicKeyValuePairTests.class);
        entries = ((TestLogger)log.getSlfjLogger()).getEntries();
    }

    @Test
    public void justKeyButNoValueTest() {
        log.error("This is an error","just_key_but_no_value");

        // does not actually generate an error, just shows the value as empty
        assertEquals(entries.toString(),1,entries.size());
        assertEquals(entries.toString(),"This is an error just_key_but_no_value=",entries.get(0).getMessage());
    }

    @Test
    public void keyWithSpacesTest() {
        log.error("This is an error","key with spaces",1L);

        // does not actually generate an error, just shows the value as empty
        assertEquals(entries.toString(),2,entries.size());
        assertEquals(entries.toString(),"Key with spaces was passed in: key with spaces",entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"This is an error",entries.get(1).getMessage());
    }

    @Test
    public void keyWithSpacesRecoverTest() {

        Throwable t = new RuntimeException("Important exception");
        TestSecurityContext toLog = new TestSecurityContext("test_user","TEST_TENANT");

        log.error("This is an error","key with spaces",1L,"good_key_that_will_be_skipped",2L,toLog,t);

        // does not actually generate an error, just shows the value as empty
        assertEquals(entries.toString(),2,entries.size());
        assertEquals(entries.toString(),"Key with spaces was passed in: key with spaces",entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        // the second key was ignored even though it was valid, we simply could not rely on the order any more with corrupted keys
        assertEquals(entries.toString(),"This is an error userName=test_user tenantId=TEST_TENANT errorMessage=\"Important exception\"",entries.get(1).getMessage());
        // validate we did not lose the exception even if it was after the key that had the error
        assertTrue(entries.toString(),entries.get(1).getError().isPresent());
    }

    @Test
    public void iToLogWithNullTest() {
        IToLog toLog = new IToLog() {
            @Override
            public Object[] toLog() {
                return null;
            }
        };

        log.error("This is an error",toLog);

        // does not actually generate an error, just shows the value as empty
        assertEquals(entries.toString(),2,entries.size());
        assertEquals(entries.toString(),"Null returned from class com.github.structlog4j.ErrorKeyValuePairTests$1.toLog()",entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"This is an error",entries.get(1).getMessage());
    }

    @Test
    public void iToLogWithWrongNumberOfParametersTest() {
        IToLog toLog = new IToLog() {
            @Override
            public Object[] toLog() {
                // do not return second key
                return new Object[]{"key1","Value1","key2"};
            }
        };

        log.error("This is an error",toLog);

        // does not actually generate an error, just shows the value as empty
        assertEquals(entries.toString(),2,entries.size());
        assertEquals(entries.toString(),"Odd number of parameters (3) returned from class com.github.structlog4j.ErrorKeyValuePairTests$2.toLog()",entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"This is an error",entries.get(1).getMessage());
    }

    @Test
    public void iToLogWithKeyWithSpacesTest() {
        IToLog toLog = new IToLog() {
            @Override
            public Object[] toLog() {
                // do not return second key
                return new Object[]{"key1","Value1","key with spaces","Value 2"};
            }
        };

        log.error("This is an error",toLog);

        // does not actually generate an error, just shows the value as empty
        assertEquals(entries.toString(),2,entries.size());
        assertEquals(entries.toString(),"Key with spaces was passed in from class com.github.structlog4j.ErrorKeyValuePairTests$3.toLog(): key with spaces",entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"This is an error key1=Value1",entries.get(1).getMessage());
    }


    @Test
    public void iToLogWithNullKeyTest() {
        IToLog toLog = new IToLog() {
            @Override
            public Object[] toLog() {
                // do not return second key
                return new Object[]{"key1","Value1",null,"Value 2"};
            }
        };

        log.error("This is an error",toLog);

        // does not actually generate an error, just shows the value as empty
        assertEquals(entries.toString(),2,entries.size());
        assertEquals(entries.toString(),"Non-String or null key was passed in from class com.github.structlog4j.ErrorKeyValuePairTests$4.toLog(): null (null)",entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"This is an error key1=Value1",entries.get(1).getMessage());
    }
}
