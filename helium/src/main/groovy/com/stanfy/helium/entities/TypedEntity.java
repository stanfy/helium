package com.stanfy.helium.entities;

import com.stanfy.helium.model.Type;

import java.util.Collections;
import java.util.List;

/**
 * Pair of type and value.
 */
public class TypedEntity<T extends Type> {

  /** Type. */
  private final T type;
  /** Value. */
  private final Object value;
  /** Validation error that may occur during creating entity value. */
  private ValidationError error;

  public TypedEntity(final T type, final Object value) {
    this.type = type;
    this.value = value;
  }

  public ValidationError getValidationError() {
    return error;
  }

  public T getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public void setValidationError(final ValidationError validationError) {
    this.error = validationError;
  }

}
