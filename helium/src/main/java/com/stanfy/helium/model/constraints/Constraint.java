package com.stanfy.helium.model.constraints;

/**
 * Constraint that can be applied to a value.
 * @param <T> value type
 */
public interface Constraint<T> {

  boolean validate(T value);

  String describe(T value);

}
