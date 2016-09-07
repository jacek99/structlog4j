package org.slf4j.impl;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;

import java.util.LinkedList;
import java.util.Optional;

/**
 * Test logger for unit tests
 */
@Value
public class TestLogger implements Logger {

    private String name;

    private LinkedList<LogEntry> entries = new LinkedList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(String msg) {
        entries.add(new LogEntry(Level.TRACE,msg, Optional.empty()));
    }

    @Override
    public void trace(String format, Object arg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void trace(String format, Object... arguments) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void trace(String msg, Throwable t) {
        entries.add(new LogEntry(Level.TRACE,msg,Optional.of(t)));
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        throw new RuntimeException("Not used");
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String msg) {
        entries.add(new LogEntry(Level.DEBUG,msg,Optional.empty()));
    }

    @Override
    public void debug(String format, Object arg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void debug(String format, Object... arguments) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void debug(String msg, Throwable t) {
        entries.add(new LogEntry(Level.DEBUG,msg,Optional.of(t)));
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public void debug(Marker marker, String msg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        throw new RuntimeException("Not used");
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        entries.add(new LogEntry(Level.INFO,msg,Optional.empty()));
    }

    @Override
    public void info(String format, Object arg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void info(String format, Object... arguments) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void info(String msg, Throwable t) {
        entries.add(new LogEntry(Level.INFO,msg,Optional.of(t)));
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public void info(Marker marker, String msg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        throw new RuntimeException("Not used");
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        entries.add(new LogEntry(Level.WARN,msg,Optional.empty()));
    }

    @Override
    public void warn(String format, Object arg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void warn(String format, Object... arguments) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void warn(String msg, Throwable t) {
        entries.add(new LogEntry(Level.WARN,msg,Optional.of(t)));
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public void warn(Marker marker, String msg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        throw new RuntimeException("Not used");
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String msg) {
        entries.add(new LogEntry(Level.ERROR,msg,Optional.empty()));
    }

    @Override
    public void error(String format, Object arg) {
        // used in internal errors
        format = format.replace("{}","%s");
        entries.add(new LogEntry(Level.ERROR,String.format(format,arg), Optional.empty()));
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        // used in internal errors
        format = format.replace("{}","%s");
        entries.add(new LogEntry(Level.ERROR,String.format(format,arg1, arg2), Optional.empty()));
    }

    @Override
    public void error(String format, Object... arguments) {
        // used in internal errors
        format = format.replace("{}","%s");
        entries.add(new LogEntry(Level.ERROR,String.format(format,arguments), Optional.empty()));
    }

    @Override
    public void error(String msg, Throwable t) {
        entries.add(new LogEntry(Level.ERROR,msg,Optional.of(t)));
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    public void error(Marker marker, String msg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        throw new RuntimeException("Not used");
    }
}
