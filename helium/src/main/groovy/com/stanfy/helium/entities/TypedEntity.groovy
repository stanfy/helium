package com.stanfy.helium.entities

import com.stanfy.helium.entities.validation.ValidationError
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

  /** Validation errors that may occur during creating entity value. */
  List<ValidationError> validationErrors

  public TypedEntity(final Type type, final Object value) {
    this.type = type
    this.value = value
  }

  List<ValidationError> getValidationErrors() {
    if (this.validationErrors == null) { return Collections.emptyList() }
    return Collections.unmodifiableList(this.validationErrors)
  }

}
