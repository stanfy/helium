package com.stanfy.helium.internal.model.tests

import com.stanfy.helium.internal.MethodsExecutor
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Checkable
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.tests.BehaviourSuite
import com.stanfy.helium.utils.DslUtils

import static com.stanfy.helium.internal.model.tests.Util.errorStack
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED

final class BehaviourDescription implements Checkable {

  ProjectDsl project
  Service service
  String name
  Closure<Void> action

  @Override
  BehaviourSuite check(final MethodsExecutor executor) {
    final  BehaviourSuite suite
    try {
      CheckBuilder builder = new CheckBuilder(project, service, executor)
      DslUtils.runWithProxy(builder, action, builder.itArgument())
      suite = new CheckGroup(builder.makeChecks(), executor).run(name)
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
