package com.stanfy.helium.internal.dsl.scenario

import com.stanfy.helium.dsl.MethodExecutionResult
import com.stanfy.helium.model.tests.Scenario
import com.stanfy.helium.internal.utils.DslUtils
import groovy.transform.CompileStatic

/**
 * Helper methods for invoking scenario.
 */
@Deprecated
class ScenarioInvoker {

  /** Current scenario delegate. */
  private static ThreadLocal<ScenarioDelegate> currentDelegate = new ThreadLocal<>();

  @CompileStatic
  public static ScenarioDelegate getDelegate() {
    return currentDelegate.get()
  }

  public static Object invokeScenario(final ScenarioDelegate delegate, final Scenario scenario) {
    Object result = null
    try {
      currentDelegate.set(delegate)
      if (scenario.before) {
        DslUtils.runWithProxy(delegate, scenario.before)
      }

      result = DslUtils.runWithProxy(delegate, scenario.action)

    } catch (Throwable e) {
      delegate.reportedProblems.add e
    } finally {
      if (scenario.after) {
        try {
          DslUtils.runWithProxy(delegate, scenario.after)
        } catch (Throwable e) {
          delegate.reportedProblems.add e
        }
      }
      currentDelegate.set(null)
    }

    def errors = delegate.intermediateResults.collect() { MethodExecutionResult r ->
      r.interactionErrors
    }.flatten().collect { def e -> e.message }
    errors += delegate.reportedProblems.collect {
      if (!it.message) {
        throw it
      }
      it.message
    }

    if (!errors.empty) {
      String errorsMessage = errors.inject("") { String res, String message ->
        res + '======================\n' + message + '\n'
      }
      throw new AssertionError("\nScenario execution faced with such errors:\n\n" + errorsMessage)
    }

    return result;
  }

}
