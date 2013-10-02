package com.stanfy.helium.dsl

import com.stanfy.helium.utils.ConfigurableProxy
import com.stanfy.helium.model.ServiceMethod

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Extended proxy for ServiceMethod.
 */
class ConfigurableServiceMethod extends ConfigurableProxy<ServiceMethod> {

  static {
    ["parameters", "response", "body"].each {
      ConfigurableServiceMethod.metaClass."$it" << { Object arg ->
        if (arg instanceof Closure<?>) {
          delegate.defineMessageType(it, (Closure<?>)arg)
        } else if (arg instanceof String) {
          delegate.defineMessageType(it, (String)arg)
        }
      }
    }
  }

  ConfigurableServiceMethod(final ServiceMethod core, final ProjectDsl project) {
    super(core, project)
  }

  void defineMessageType(final String property, Closure<?> body) {
    ServiceMethod core = getCore()
    core."$property" = getProject().createAndAddMessage("${core.canonicalName}_${property}_${core.type}", body, false)
  }

  void defineMessageType(final String property, String messageType) {
    ServiceMethod core = getCore()
    core."$property" = getProject().types.byName(messageType)
  }

  void tests(final Closure<?> spec) {
    runWithProxy(new ConfigurableMethodTestsInfo(getCore().testInfo, getProject()), spec)
  }

}
