package com.stanfy.helium.internal.utils;

import java.util.Map;

/**
 * Proxy for a map. Allows to construct map with syntax like
 * <code>
 *   'key1' 'value1'
 *   'key2' 'value2'
 * </code>
 */
final class ConfigurableStringMap extends ConfigurableGenericMap<String, String> {

  ConfigurableStringMap(final Map<String, String> map, final String name, final Map<String, Object> scope) {
    super(map, name, String.class, scope);
  }

  @Override
  protected String resolveValue(final String key, final Object arg) {
    String value = String.valueOf(arg);
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException(
          "Values of '" + this.name + "' cannot be empty. Cannot set value for '" + key + "'."
      );
    }
    return value;
  }

}
