package com.stanfy.helium.internal.model.tests

import com.stanfy.helium.model.Checkable
import com.stanfy.helium.model.tests.BehaviourSuite

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.PASSED

final class BehaviourDescription implements Checkable {

  String name
  Closure<Void> action

  @Override
  BehaviourSuite check() {
    StringBuilder description = new StringBuilder()
    BehaviourSuite suite = new BehaviourSuite(name: name)
    try {
      action()
      suite.result = PASSED
    } catch (Throwable e) {
      suite.result = FAILED
      description.append("Unexpected error\n").append(errorStack(e))
    }
    suite.description = description.toString()
    return suite
  }

  private static String errorStack(final Throwable e) {
    StringWriter out = new StringWriter()
    e.printStackTrace(new PrintWriter(out))
    return out.toString()
  }

}
