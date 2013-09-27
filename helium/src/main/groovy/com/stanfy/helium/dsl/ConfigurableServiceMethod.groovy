package com.stanfy.helium.dsl

import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.tests.MethodTestInfo
import com.stanfy.helium.model.tests.TestsInfo

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
    ConfigurableServiceMethod.metaClass.tests << { Closure<?> description ->
      delegate.defineTestsInfo description
    }
  }

  ConfigurableServiceMethod(final ServiceMethod core, final ProjectDsl project) {
    super(core, project)
  }

  void defineMessageType(final String property, Closure<?> body) {
    ServiceMethod core = getCore()
    core."$property" = getProject().createAndAddMessage("${core.canonicalName}_$property", body, false)
  }

  void defineMessageType(final String property, String messageType) {
    ServiceMethod core = getCore()
    core."$property" = getProject().types.byName(messageType)
  }

  void defineTestsInfo(final Closure<?> spec) {
    ProjectDsl.callConfigurationSpec(new ConfigurableProxy<MethodTestInfo>(getCore().testInfo, getProject()), spec)
  }

}
