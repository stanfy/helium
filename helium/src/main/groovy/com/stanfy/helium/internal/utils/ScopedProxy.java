package com.stanfy.helium.internal.utils;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingFieldException;
import groovy.lang.MissingPropertyException;

import java.util.Map;

abstract class ScopedProxy extends GroovyObjectSupport {

  /** Variables storage. */
  private final Map<String, Object> scope;

  public ScopedProxy(final Map<String, Object> scope) {
    this.scope = scope;
  }

  protected Object doGetProperty(final String name) {
    return super.getProperty(name);
  }

  @Override
  public Object getProperty(final String name) {
    try {
      return doGetProperty(name);
    } catch (MissingFieldException e) {
      if (e.getField().equals(name) && scope.containsKey(name)) {
        return scope.get(name);
      }
      throw e;
    } catch (MissingPropertyException e) {
      if (e.getProperty().equals(name) && scope.containsKey(name)) {
        return scope.get(name);
      }
      throw e;
    }
  }

}
