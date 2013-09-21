package com.stanfy.helium.model

/**
 * Configurable entity.
 */
class ConfigurableEntity {

  void configure(Closure<?> config) {
    config.delegate = this
    config.resolveStrategy = Closure.DELEGATE_FIRST
    config.call()
  }

  @Override
  Object invokeMethod(final String name, final Object args) {
    if (hasProperty(name)) {
      if (args.getClass().isArray()) {
        if (args.length != 1) { throw new IllegalArgumentException("Bad arguments $args for property $name") }
        this."$name" = args[0]
      } else {
        this."$name" = args
      }
      return this."$name"
    }
    throw new MissingMethodException(name, getClass(), args)
  }

}
