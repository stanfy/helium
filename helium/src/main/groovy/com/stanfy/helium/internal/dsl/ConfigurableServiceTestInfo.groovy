package com.stanfy.helium.internal.dsl

import com.stanfy.helium.model.tests.Scenario
import com.stanfy.helium.model.tests.ServiceTestInfo

/**
 * Proxy for ServiceTestInfo.
 */
class ConfigurableServiceTestInfo extends ConfigurableTestsInfo<ServiceTestInfo> {

  ConfigurableServiceTestInfo(final ServiceTestInfo core, final ProjectDsl project) {
    super(core, project)
  }

  def scenario(final Map<String, Closure<?>> actions, final String name) {
    return [
        spec : { Closure<?> mainAction ->
          Closure<?> before = actions.before
          Closure<?> after = actions.after
          Scenario test = new Scenario(name: name, action: mainAction, before: before, after: after)
          ServiceTestInfo core = getCore()
          core.addScenario(test)
        }
    ]
  }

  def scenario(final String name) {
    return [
        spec : { Closure<?> mainAction ->
          ServiceTestInfo core = getCore()
          core.addScenario(new Scenario(name: name, action: mainAction))
        }
    ]
  }
}
