package com.stanfy.helium.utils;

import groovy.lang.Closure;

/**
 * DSL utils.
 */
public class DslUtils {

  private DslUtils() { /* hidden */ }

  public static <T> T runWithProxy(final Object proxy, final Closure<T> closure) {
    @SuppressWarnings("unchecked")
    Closure<T> body = (Closure<T>) closure.clone();
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.setDelegate(proxy);
    return body.call();
  }

}
