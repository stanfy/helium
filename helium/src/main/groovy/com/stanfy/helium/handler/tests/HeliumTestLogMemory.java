package com.stanfy.helium.handler.tests;

import com.stanfy.helium.model.tests.BehaviourCheck;

import java.util.HashMap;
import java.util.Map;

abstract class HeliumTestLogMemory implements HeliumTestLog {

  private final Map<BehaviourCheck, StringBuilder> outputs = new HashMap<BehaviourCheck, StringBuilder>();

  @Override
  public final void write(final String fmt, final Object... args) {
    BehaviourCheck check = currentCheck();
    if (check == null) {
      throw new IllegalStateException("No current output");
    }
    StringBuilder sb = outputs.get(check);
    if (sb == null) {
      sb = new StringBuilder();
      outputs.put(check, sb);
    }
    if (args.length > 0) {
      sb.append(String.format(fmt, args));
    } else {
      sb.append(fmt);
    }
    sb.append('\n');
  }

  protected abstract BehaviourCheck currentCheck();

  protected final String log(final BehaviourCheck check) {
    StringBuilder sb = outputs.get(check);
    if (sb == null) {
      return "";
    }
    return sb.toString();
  }

}
