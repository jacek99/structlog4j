package com.github.structlog4j.test.samples;

import com.github.structlog4j.IToLog;
import lombok.Value;

/** A Sample of an object that implements the IToLog */
@Value
public class TestSecurityContext implements IToLog {

  private String userName;
  private String tenantId;

  @Override
  public Object[] toLog() {
    return new Object[] {"userName", getUserName(), "tenantId", getTenantId()};
  }
}
