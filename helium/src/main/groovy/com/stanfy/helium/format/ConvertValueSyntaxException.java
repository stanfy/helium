package com.stanfy.helium.format;

/**
 * Exception thrown by converters when value was read from the input, but
 * it could not be converted to a target type.
 */
public class ConvertValueSyntaxException extends Exception {

  /** Bad value. */
  private final Object value;

  public ConvertValueSyntaxException(final Object value, final String message) {
    super("Cannot convert value <" + value + ">. " + message);
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

}
