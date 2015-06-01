package com.stanfy.helium.utils;

import groovy.lang.Closure;

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

}
