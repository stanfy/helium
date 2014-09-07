package com.stanfy.helium.dsl

import com.stanfy.helium.model.constraints.Constraint
import com.stanfy.helium.model.constraints.EnumConstraint

/**
 * DSL for building constraints.
 */
final class ConstraintsDsl {

  /** Collection to modify. */
  private final Collection<Constraint<?>> constraints;

  public ConstraintsDsl(final Collection<Constraint<?>> target) {
    this.constraints = target;
  }

  void enumeration(final Collection<Object> values) {
    constraints.add(new EnumConstraint<Object>(new HashSet<Object>(values)))
  }

  void enumeration(final Object... values) {
    enumeration(Arrays.asList(values))
  }

}
