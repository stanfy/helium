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
  /** Validation errors that may occur during creating entity value. */
  private List<ValidationError> validationErrors;

  public TypedEntity(final T type, final Object value) {
    this.type = type;
    this.value = value;
  }

  public List<ValidationError> getValidationErrors() {
    if (this.validationErrors == null) {
      return Collections.emptyList();
    }

    return Collections.unmodifiableList(this.validationErrors);
  }

  public T getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public void setValidationErrors(final List<ValidationError> validationErrors) {
    this.validationErrors = validationErrors;
  }

}
