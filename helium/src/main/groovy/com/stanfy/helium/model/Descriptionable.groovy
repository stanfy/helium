package com.stanfy.helium.model

import com.stanfy.helium.utils.Names
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

  String getCanonicalName() {
    return Names.canonicalName(name)
  }

  @Override
  String toString() {
    return "${getClass().simpleName}($name)"
  }

  void setDescription(final String desc) {
    this.@description = desc ? desc.trim() : null
  }

}
