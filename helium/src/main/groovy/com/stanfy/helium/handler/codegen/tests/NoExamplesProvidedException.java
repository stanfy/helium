package com.stanfy.helium.handler.codegen.tests;

import com.stanfy.helium.model.Field;

/**
 * Exception thrown when there are no required examples for processing some type.
 */
public class NoExamplesProvidedException extends Exception {

  /** Field. */
  private final Field field;

  public NoExamplesProvidedException(final Field field, final String message) {
    super("No examples provided for " + field + (message != null ?  ". " + message : ""));
    this.field = field;
  }

  public Field getField() {
    return field;
  }

}
