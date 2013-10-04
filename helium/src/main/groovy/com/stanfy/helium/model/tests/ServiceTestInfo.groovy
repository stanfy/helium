package com.stanfy.helium.model.tests

/**
 * Service test information.
 */
class ServiceTestInfo extends TestsInfo {

  /** List of test scenarios. */
  final List<Scenario> scenarios = new ArrayList<>()

  List<Scenario> getScenarios() { return Collections.unmodifiableList(scenarios) }

  void addScenario(final Scenario scenario) {
    scenarios.add(scenario)
  }

  Scenario scenarioByName(final String name) {
    return scenarios.find { it.name == name }
  }

}
