package com.stanfy.helium.model.constraints

import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Spec for Constrained type.
 */
class ConstrainedTypeTest extends Specification {

  private ConstrainedType type

  def setup() {
    type = new ConstrainedType(new Type(name: "string"))
  }

  def "building constraints"() {
    when:
    type.addConstraint(new EnumConstraint<String>(['a'] as Set))
    type.addConstraints([new EnumConstraint<String>(['b'] as Set)])
    then:
    type.constraints.size() == 2
  }

  def "empty constraints"() {
    expect:
    type.constraints.empty
  }

}
