package com.stanfy.helium.dsl

import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod

/**
 * Extended proxy for services configuration.
 */
class ConfigurableService extends ConfigurableProxy<Service> {

  static {
    MethodType.values().each { MethodType type ->
      ConfigurableService.metaClass."${type.toString().toLowerCase(Locale.US)}" << { Object arg ->
        String path = "$arg"
        return [
            "spec" : { Closure<?> spec -> delegate.addServiceMethod(path, type, spec) }
        ]
      }
    }
  }

  ConfigurableService(final Service core, final Project project) {
    super(core, project)
  }

  ServiceMethod addServiceMethod(final String path, final MethodType type, Closure<?> spec) {
    ServiceMethod method = new ServiceMethod(path: path, type: type)

    Closure<?> body = spec.clone() as Closure<?>
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = new ConfigurableServiceMethod(method, getProject())
    body.call()

    getCore().methods.add method
    return method
  }

}
