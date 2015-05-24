package com.stanfy.helium.internal.model.tests

import com.stanfy.helium.model.Checkable
import com.stanfy.helium.model.tests.BehaviourSuite
import com.stanfy.helium.utils.DslUtils

import static com.stanfy.helium.internal.model.tests.Util.errorStack
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED

final class BehaviourDescription implements Checkable {

  String name
  Closure<Void> action

  @Override
  BehaviourSuite check() {
    final  BehaviourSuite suite
    try {
      CheckBuilder builder = new CheckBuilder()
      DslUtils.runWithProxy(builder, action, builder.itArgument())
      suite = new CheckGroup(builder.makeChecks()).run(name)
    } catch (Throwable e) {
      suite = new BehaviourSuite(
          name: name,
          result: FAILED,
          description: errorStack(e)
      )
    }
    return suite
  }

}
