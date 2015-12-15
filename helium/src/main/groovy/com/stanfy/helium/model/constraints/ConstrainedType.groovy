package com.stanfy.helium.model.constraints

import com.stanfy.helium.model.Type
import groovy.transform.CompileStatic

/**
 * Type with some constraints - subtype.
 */
@CompileStatic
final class ConstrainedType extends Type {

  /** Base type. */
  private final Type baseType

  /** Applied constraints. */
  private final ArrayList<Constraint> constraints = new ArrayList<>()

  public ConstrainedType(final Type baseType) {
    if (!baseType.primitive) {
      throw new IllegalArgumentException("Base type must be a primitive")
    }
    this.baseType = baseType
  }

  @Override
  boolean isPrimitive() {
    return true
  }

  Type getBaseType() {
    return baseType
  }

  void addConstraint(final Constraint<?> constraint) {
    this.constraints.add(constraint)
  }

  void addConstraints(final Collection<Constraint<?>> constraints) {
    this.constraints.addAll(constraints)
  }

  Collection<Constraint<Object>> getConstraints() {
    return Collections.unmodifiableList(constraints)
  }

  boolean containsConstraint(final Class<? extends Constraint> constraint) {
    return getConstraint(constraint) != null
  }

  Constraint<?> getConstraint(final Class<?> constraint) {
    return this.@constraints.find { constraint.isAssignableFrom(it.getClass()) }
  }

}
