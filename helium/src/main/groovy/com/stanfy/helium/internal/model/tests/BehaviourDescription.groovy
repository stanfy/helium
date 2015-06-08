package com.stanfy.helium.internal.model.tests

import com.stanfy.helium.internal.MethodsExecutor
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Checkable
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.tests.BehaviourSuite
import com.stanfy.helium.model.tests.CheckListener
import com.stanfy.helium.utils.DslUtils
import org.joda.time.Duration

import static com.stanfy.helium.internal.model.tests.Util.errorStack
import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED

final class BehaviourDescription implements Checkable {

  private static final CheckBuilderStack BUILDERS = new CheckBuilderStack();

  ProjectDsl project
  Service service
  String name
  Closure<Void> action

  static CheckBuilder currentBuilder() {
    def stack = BUILDERS.get()
    return stack.empty ? null : stack.last()
  }

  @Override
  BehaviourSuite check(final MethodsExecutor executor, final CheckListener listener) {
    long startTime = System.currentTimeMillis()
    boolean errored = false
    BehaviourSuite res = null
    try {
      CheckBuilder builder = new CheckBuilder(project, service, executor, listener)
      BUILDERS.get().add(builder)
      DslUtils.runWithProxy(builder, action, builder.itArgument())
      return (res = new CheckGroup(builder.makeChecks(), executor, listener).run(name))
    } catch (Throwable e) {
      errored = true
      res = new BehaviourSuite(name: name, description: errorStack(e), result: FAILED)
      listener.onSuiteStarted(res)
      return res
    } finally {
      res.setTime(Duration.millis(System.currentTimeMillis() - startTime))
      def stack = BUILDERS.get()
      stack.remove(stack.size() - 1)
      if (errored) {
        listener.onSuiteDone(res)
      }
    }
  }

  private static class CheckBuilderStack extends ThreadLocal<ArrayList<CheckBuilder>> {
    protected ArrayList<CheckBuilder> initialValue() {
      return new ArrayList<CheckBuilder>(5);
    }
  }

}
