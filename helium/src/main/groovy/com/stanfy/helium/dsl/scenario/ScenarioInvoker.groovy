package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.model.tests.Scenario
import com.stanfy.helium.utils.DslUtils

/**
 * Helper methods for invoking scenario.
 */
class ScenarioInvoker {

  public static Object invokeScenario(final ScenarioDelegate delegate, final Scenario scenario) {
    AssertionError crucialError = null
    Object result = null
    try {
      if (scenario.before) {
        DslUtils.runWithProxy(delegate, scenario.before)
      }

      result = DslUtils.runWithProxy(delegate, scenario.action)

      if (scenario.after) {
        DslUtils.runWithProxy(delegate, scenario.after)
      }
    } catch (AssertionError e) {
      crucialError = e
    }

    def errors = delegate.intermediateResults.collect() { MethodExecutionResult r ->
      r.interactionErrors
    }.flatten().collect() { AssertionError e -> e.message }
    if (crucialError) {
      errors += crucialError.message
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
