package com.stanfy.helium.dsl

import groovy.transform.CompileStatic

/**
 * Proxy for a map. Allows to construct map with syntax like
 * <code>
 *   'key1' 'value1'
 *   'key2' 'value2'
 * </code>
 */
@CompileStatic
class ConfigurableStringMap {

  /** Core map. */
  private final Map<String, String> map

  /** Name used to describe errors. */
  private final String name

  public ConfigurableStringMap(final Map<String, String> map, final String name) {
    this.map = map
    this.name = name
  }

  @Override
  Object invokeMethod(final String name, final Object args) {
    Object arg = ConfigurableProxy.resolveSingleArgument("$name in ${this.name}", args)
    if (!(arg instanceof String)) {
      throw new IllegalArgumentException("Values of ${this.name} must be strings. Got: $arg of type ${arg.class} for $name")
    }
    if (map.containsKey(name)) {
      throw new IllegalArgumentException("Key $name is already defined in ${this.name}")
    }
    String value = (String) arg
    if (!value) { throw new IllegalArgumentException("Values of ${this.name} cannot be empty") }
    map[name] = value
  }

}
