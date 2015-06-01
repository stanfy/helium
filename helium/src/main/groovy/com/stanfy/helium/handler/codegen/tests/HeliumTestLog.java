package com.stanfy.helium.handler.codegen.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

interface HeliumTestLog {

  void write(String fmt, Object... args);

  class DefaultLogger implements HeliumTestLog {
    private static final Logger LOG = LoggerFactory.getLogger(HeliumTestLog.class);

    @Override
    public void write(final String fmt, final Object... args) {
      if (args.length == 0) {
        LOG.info(fmt);
      } else {
        LOG.info(String.format(fmt, args));
      }
    }
  }

}
