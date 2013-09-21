package com.stanfy.helium.dsl

import com.stanfy.helium.model.Message
import com.stanfy.helium.model.ServiceMethod

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

  ConfigurableServiceMethod(final ServiceMethod core, final Dsl dsl) {
    super(core, dsl)
  }

  void defineMessageType(final String property, Closure<?> body) {
    ServiceMethod core = getCore()
    core."$property" = getDsl().createAndAddMessage("${core.name}_$property", body)
  }

  void defineMessageType(final String property, String messageType) {
    ServiceMethod core = getCore()
    core."$property" = (Message) getDsl().types.byName(messageType)
  }

}
