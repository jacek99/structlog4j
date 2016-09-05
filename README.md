# StructLog4J

Structured logging Java, on top of the SLF4J API.
Designed to generate easily parsable log messages for consumption in services such as LogStash, Splunk, ElasticSearch, etc.

# Overview

Standard Java messages look something like this

    Handled 4 events while processing the import batch processing

This is human readable, but very difficult to parse by code in a log aggergation serivce, as every developer
can enter any free form text they want, in any format.

Instead, we can generated a message like this

   Handled events service="Import Batch" numberOfEvents=4

This is very easy to parse, the message itself is just a plain description and all the context information is
passed as separate key/value pairs (or in the future JSON, YAML, etc)

# Usage

StructLog4J is implemented itself on top of the SLF4J API. Therefore any application that already uses SLF4J can
start using it immediately as it requires no other changes to existing logging configuration.

Instead of the standard SLF4J Logger, you must instantiate instead the StructLog4J Logger:

    private ILogger log = SLoggerFactory.getLogger(MyClass.class);

The **ILogger** interface is very simple and offers just these basic methods:

    public interface ILogger {
        public void error(String message, Object...params);
        public void warn(String message, Object...params);
        public void info(String message, Object...params);
        public void debug(String message, Object...params);
        public void trace(String message, Object...params);
    }

## Logging key value pairs

Just pass in key/value pairs as parameters (all keys **must** be String, values can be anything), e.g.

    log.info("Starting processing","user",securityContext.getPrincipal().getName(),"tenanId",securityContext.getTenantId());

which would generate a log message like:

    Starting processing user=johndoe@gmail.com tenantId=SOME_TENANT_ID

## Logging exceptions

There is no separate API for Throwable (like in SLF4j), just pass in the exception as one of the parameters (order is not
important) and we will log its root cause message and the entire exception stack trace:

    } catch (Exception e) {
        log.error("Error occcurred during batch processing","user",securityContext.getPrincipal().getName(),"tenanId",securityContext.getTenantId(), e);
    }

which would generate a log message like:

    Error occurreded during batch processing user=johndoe@gmail.com tenantId=SOME_TENANT_ID errorMessage="ORA-14094: Oracle Hates You"
    ...followed by regular full stack trace of the exception...

## Enforcing custom logging format per object

If you wish, any POJO in your app can implement the **IToLog** interface, e.g.

    public class TenantSecurityContext implements IToLog {

        private String userName;
        private String tenantId;

        @Override
        public Object[] toLog() {
            return new Object[]{"userName",getUserName(),"tenantId",getTenantId()};
        }
    }

Then you can just pass in the object instance directly, without the need to specify any key/value pairs:

    log.info("Starting processing",securityContext);

and that will generate a log entry like:

    Starting processing user=johndoe@gmail.com tenantId=SOME_TENANT_ID

## All together now

You can mix and match all of these together without any issues:

        } catch (Exception e) {
            log.error("Error occcurred during batch processing",securityContext, e, "hostname", InetAddress.getLocalHost().getHostName());
        }

and you would get:

    Error occurreded during batch processing user=johndoe@gmail.com tenantId=SOME_TENANT_ID errorMessage="ORA-14094: Oracle Hates You" hostname=DEV_SERVER1
    ...followed by regular full stack trace of the exception...

# License

MIT License.

Use it at will, just don't sue me.

# Dependencies

The only dependency is the SLF4 API (MIT License as well).

That's it. This library is geared towards easy integration into existing applications with strict license review process.

