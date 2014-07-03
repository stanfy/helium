package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.model.tests.Scenario
import spock.lang.Specification

/**
 * @author Olexandr Tereshchuk - "Stanfy"
 * @since 03.07.14
 */
class ScenarioInvokerTest extends Specification {

  ScenarioDelegate scenarioDelegate = new ScenarioDelegate(null, null)

  def "always execute 'after' after failed 'action'"() {
    given:
    def executed = false
    Scenario scenario = new Scenario(action: { throw new AssertionError("fail") }, after: { executed = true })

    when:
    ScenarioInvoker.invokeScenario(scenarioDelegate, scenario)

    then:
    executed
    thrown AssertionError
  }

  def "always execute 'after' after successful 'action'"() {
    given:
    def executed = false
    Scenario scenario = new Scenario(action: { }, after: { executed = true })

    when:
    ScenarioInvoker.invokeScenario(scenarioDelegate, scenario)

    then:
    executed
  }
}
