package com.github.structlog4j.yaml.test;

import com.github.structlog4j.IToLog;
import com.github.structlog4j.SLogger;
import com.github.structlog4j.SLoggerFactory;
import com.github.structlog4j.StructLog4J;
import com.github.structlog4j.json.JsonFormatter;
import com.github.structlog4j.test.TestUtils;
import com.github.structlog4j.test.samples.TestSecurityContext;
import com.github.structlog4j.yaml.YamlFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
 * Tests for handling of invalid input
 */
public class ErrorYamlTests {

    private SLogger log;
    private LinkedList<LogEntry> entries;

    @Before
    public void setup() {
        TestUtils.initForTesting();
        StructLog4J.setFormatter(YamlFormatter.getInstance());

        log = (SLogger) SLoggerFactory.getLogger(ErrorYamlTests.class);
        entries = ((TestLogger)log.getSlfjLogger()).getEntries();
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidYaml() {
        // ensure our Yaml validation logic actually works
        List<LogEntry> entries = new ArrayList<>();
        entries.add(new LogEntry(Level.ERROR,"{corrupted JSON]", Optional.empty()));
        assertYamlMessage(entries,0);
    }

    @Test
    public void justKeyButNoValueTest() {
        log.error("This is an error","just_key_but_no_value");

        // does not actually generate an error, just shows the value as empty
        assertEquals(entries.toString(),1,entries.size());
        // the key with missing value was not logged
        assertEquals(entries.toString(),"message: This is an error",entries.get(0).getMessage());
    }

    @Test
    public void keyWithSpacesTest() {
        log.error("This is an error","key with spaces",1L);

        // does not actually generate an error, just shows the value as empty
        assertEquals(entries.toString(),2,entries.size());
        assertEquals(entries.toString(),"Key with spaces was passed in: key with spaces",entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"message: This is an error",entries.get(1).getMessage());
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
        assertEquals(entries.toString(),"tenantId: TEST_TENANT\n" +
                "errorMessage: Important exception\n" +
                "message: This is an error\n" +
                "userName: test_user",entries.get(1).getMessage());
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
        assertEquals(entries.toString(),"Null returned from class com.github.structlog4j.yaml.test.ErrorYamlTests$1.toLog()"
                ,entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"message: This is an error",entries.get(1).getMessage());
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
        assertEquals(entries.toString(),"Odd number of parameters (3) returned from class com.github.structlog4j.yaml.test.ErrorYamlTests$2.toLog()",
                entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"message: This is an error",entries.get(1).getMessage());
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
        assertEquals(entries.toString(),"Key with spaces was passed in from class com.github.structlog4j.yaml.test.ErrorYamlTests$3.toLog(): key with spaces",entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"key1: Value1\n" +
                "message: This is an error",entries.get(1).getMessage());
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
        assertEquals(entries.toString(),"Non-String or null key was passed in from class com.github.structlog4j.yaml.test.ErrorYamlTests$4.toLog(): null (null)",entries.get(0).getMessage());

        // validate that despite the error we still managed to process the log entry and logged as much as we could
        assertEquals(entries.toString(),"key1: Value1\n" +
                "message: This is an error",entries.get(1).getMessage());
    }

    @Test
    public void duplicateMessageKeyTest() {
        log.error("This is a message","message","This is another message");

        assertEquals(entries.toString(),2,entries.size());
        assertMessage(entries,0, Level.WARN,"Key 'message' renamed to 'message2' in order to avoid overriding default YAML message field. Please correct in your code.",false);

        assertYamlMessage(entries,1);
        assertMessage(entries,1, Level.ERROR,"message: This is a message\n" +
                "message2: This is another message",false);
    }

    @Test
    public void valueWithQuotes() {
        log.error("This is a message","key1","Some \" value with \" quotes");

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0, Level.ERROR,"key1: Some \" value with \" quotes\n" +
                "message: This is a message",false);
    }

    @Test
    public void messageWithQuotes() {
        log.error("This is a \"message\"","key1","Some \" value with \" quotes");

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0, Level.ERROR,"key1: Some \" value with \" quotes\n" +
                "message: This is a \"message\"",false);
    }

    @Test
    public void keyWithQuotes() {
        // you are sick if you try to do this in your code...but let's be sure just in case
        log.error("This is message","key\"1","Some value");

        assertEquals(entries.toString(),1,entries.size());
        assertYamlMessage(entries,0);
        assertMessage(entries,0, Level.ERROR,"message: This is message\n" +
                "key\"1: Some value",false);
    }
}

