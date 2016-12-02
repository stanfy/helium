package com.stanfy.helium.internal.utils;

import groovy.lang.Closure;

import java.util.Collections;
import java.util.Map;

/**
 * DSL utils.
 */
public final class DslUtils {

  private DslUtils() { /* hidden */ }

  public static <T> T runWithProxy(final Object proxy, final Closure<T> closure, final Object... args) {
    @SuppressWarnings("unchecked")
    Closure<T> body = (Closure<T>) closure.clone();
    body.setDelegate(proxy);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    return body.call(args);
  }

  public static ConfigurableGenericMap<String, String> stringMapProxy(final Map<String, String> map,
                                                                      final String name) {
    return stringMapProxy(map, name, Collections.<String, Object>emptyMap());
  }

  public static ConfigurableGenericMap<String, String> optionalStringMapProxy(final Map<String, String> map,
                                                                              final String name) {
    return new ConfigurableStringMap(map, name, Collections.<String, Object>emptyMap(), true);
  }

  public static ConfigurableGenericMap<String, String> stringMapProxy(final Map<String, String> map,
                                                                      final String name,
                                                                      final Map<String, Object> scope) {
    return new ConfigurableStringMap(map, name, scope, false);
  }

}
