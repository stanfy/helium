package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.model.tests.Scenario
import com.stanfy.helium.utils.DslUtils
import groovy.transform.CompileStatic

/**
 * Helper methods for invoking scenario.
 */
@CompileStatic
class ScenarioInvoker {

  public static Object invokeScenario(final ScenarioDelegate delegate, final Scenario scenario) {
    if (scenario.before) {
      DslUtils.runWithProxy(delegate, scenario.before)
      throwErrors(delegate, "'Before' action");
    }

    Object result = DslUtils.runWithProxy(delegate, scenario.action)
    throwErrors(delegate, "Main action");

    if (scenario.after) {
      DslUtils.runWithProxy(delegate, scenario.after)
      throwErrors(delegate, "'After' action");
    }

    return result;
  }

  private static void throwErrors(final ScenarioDelegate delegate, final String label) {
    def errors = delegate.intermediateResults.collect() { MethodExecutionResult r ->
      r.interactionErrors
    }.flatten().collect() { AssertionError e -> e.message }

    if (!errors.empty) {
      throw new AssertionError(
          "\n$label results contain errors:\n\n"
              + errors.inject("") { String res, String message ->
            res + '======================\n' + message + '\n'
          }
      )
    }
  }

}
