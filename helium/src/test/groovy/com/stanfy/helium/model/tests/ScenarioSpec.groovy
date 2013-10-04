package com.stanfy.helium.model.tests

import spock.lang.Specification

/**
 * Spec for Scenario.
 */
class ScenarioSpec extends Specification {

  Scenario scenario = new Scenario()

  def "canonical name removes spaces"() {
    given:
    scenario.name = "login and get :)"

    expect:
    scenario.canonicalName == "login_and_get"
  }

}
