package com.stanfy.helium.internal.dsl.scenario

import com.stanfy.helium.model.tests.Scenario
import spock.lang.Specification

/**
 * @author Olexandr Tereshchuk - "Stanfy"
 * @since 03.07.14
 */
@Deprecated
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

  def "report all errors"() {
    given:
    Scenario scenario = new Scenario(
        before: { throw new AssertionError("error1") },
        action: { throw new AssertionError("will not get here") },
        after: { throw new Exception("error2") }
    )

    when:
    ScenarioInvoker.invokeScenario(scenarioDelegate, scenario)

    then:
    def e = thrown AssertionError
    e.message.contains("error1")
    e.message.contains("error2")
    !e.message.contains("will not get here")
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
