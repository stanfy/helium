package com.stanfy.helium.internal.model.tests;

import java.io.PrintWriter;
import java.io.StringWriter;

final class Util {

  private Util() { }

  static String errorStack(final Throwable e) {
    StringWriter out = new StringWriter();
    e.printStackTrace(new PrintWriter(out));
    return out.toString();
  }

}
