package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * Test Logger Factory for tests
 *
 * @author Jacek Furmankiewicz
 */
public class TestLoggerFactory implements ILoggerFactory {
  @Override
  public Logger getLogger(String name) {
    return new TestLogger(name);
  }
}
