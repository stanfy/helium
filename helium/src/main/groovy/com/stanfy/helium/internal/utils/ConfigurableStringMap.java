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

  private final boolean allowEmpty;

  ConfigurableStringMap(final Map<String, String> map, final String name, final Map<String, Object> scope,
                        final boolean allowEmpty) {
    super(map, name, String.class, scope);
    this.allowEmpty = allowEmpty;
  }

  @Override
  protected String resolveValue(final String key, final Object arg) {
    String value = String.valueOf(arg);
    if (!allowEmpty && (value == null || value.isEmpty())) {
      throw new IllegalArgumentException(
          "Values of '" + this.name + "' cannot be empty. Cannot set value for '" + key + "'."
      );
    }
    return value;
  }

}
