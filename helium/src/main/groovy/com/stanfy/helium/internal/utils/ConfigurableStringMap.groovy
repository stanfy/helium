package com.stanfy.helium.internal.utils

import groovy.transform.CompileStatic

/**
 * Proxy for a map. Allows to construct map with syntax like
 * <code>
 *   'key1' 'value1'
 *   'key2' 'value2'
 * </code>
 */
@CompileStatic
class ConfigurableStringMap extends ConfigurableMap<String> {

  public ConfigurableStringMap(final Map<String, String> map, final String name) {
    super(map, name)
  }
  public ConfigurableStringMap(final Map<String, String> map, final String name, final Map<String, Object> scope) {
    super(map, name, scope)
  }

  @Override
  protected String resolveValue(final String key, final Object arg) {
    String value = arg as String
    if (!value) { throw new IllegalArgumentException("Values of ${this.name} cannot be empty") }
    return value
  }

}
