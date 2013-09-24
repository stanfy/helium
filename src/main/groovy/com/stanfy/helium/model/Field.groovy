package com.stanfy.helium.model

import groovy.transform.CompileStatic

import java.util.regex.Pattern

/**
 * Message field.
 */
@CompileStatic
class Field extends Descriptionable {

  /** Name pattern. */
  private static final Pattern NAME_PATTERN = ~/^[a-zA-A0-9_-]+$/

  /** Field type. */
  Type type

  /** Required option, true by default. */
  boolean required = true

  @Override
  void setName(final String name) {
    if (!NAME_PATTERN.matcher(name).matches()) {
      throw new IllegalArgumentException("Name must match ${NAME_PATTERN.pattern()}")
    }
    super.setName(name)
  }

}
