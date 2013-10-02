package com.stanfy.helium.entities

import com.stanfy.helium.model.Type
import groovy.transform.CompileStatic

/**
 * Pair of type and value.
 */
@CompileStatic
class TypedEntity {

  /** Type. */
  final Type type

  /** Value. */
  final Object value

  public TypedEntity(final Type type, final Object value) {
    this.type = type
    this.value = value
  }

}
