package com.stanfy.helium.model.constraints;

import java.util.Set;

/**
 * Validates whether value belongs to a particular set of possible values.
 */
public class EnumConstraint<T> implements Constraint<T> {

  private final Set<T> values;

  public EnumConstraint(final Set<T> values) {
    this.values = values;
  }

  @Override
  public boolean validate(final T value) {
    return values.contains(value);
  }

  @Override
  public String describe(final T value) {
    return "Value " + value + " must belong to a set " + values;
  }

}
