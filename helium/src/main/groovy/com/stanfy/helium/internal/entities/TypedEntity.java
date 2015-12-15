package com.stanfy.helium.internal.entities;

import com.stanfy.helium.model.Type;

/**
 * Pair of type and value.
 */
public class TypedEntity<T extends Type> {

  /** Type. */
  private final T type;
  /** Value. */
  private final Object value;
  /** Validation error that may occur during creating entity value. */
  private final ValidationError error;

  public TypedEntity(final T type, final Object value) {
    this(type, value, null);
  }

  public TypedEntity(final T type, final Object value, final ValidationError error) {
    this.type = type;
    this.value = value;
    this.error = error;
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

}
