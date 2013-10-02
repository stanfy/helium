package com.stanfy.helium.utils

import groovy.transform.CompileStatic

/**
 * Base class for configurable maps.
 * @param <V> values type
 */
@CompileStatic
abstract class ConfigurableMap<V> {

  /** Core map. */
  protected final Map<String, V> map

  /** Name used to describe errors. */
  protected final String name

  public ConfigurableMap(final Map<String, V> map, final String name) {
    this.map = map
    this.name = name
  }

  protected abstract V resolveValue(final String key, final Object arg);

  @Override
  Object invokeMethod(final String name, final Object args) {
    if (map.containsKey(name)) {
      throw new IllegalArgumentException("Key $name is already defined in ${this.name}")
    }
    Object arg = ConfigurableProxy.resolveSingleArgument("$name in ${this.name}", args)
    V value = resolveValue(name, arg)
    map[name] = value
  }

}
