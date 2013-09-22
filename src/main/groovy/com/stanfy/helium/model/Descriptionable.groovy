package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Entity that has name and description.
 */
@CompileStatic
class Descriptionable {

  /** Name. */
  String name

  /** Description. */
  String description;

  @Override
  String toString() {
    return "${getClass().simpleName}($name)"
  }
}
