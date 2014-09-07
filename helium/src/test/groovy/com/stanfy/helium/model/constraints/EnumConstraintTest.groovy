package com.stanfy.helium.model.constraints

import spock.lang.Specification

/**
 * Tests for enum constraint.
 */
class EnumConstraintTest extends Specification {

  def "string enums"() {
    given:
    def constraint = new EnumConstraint<String>(['a', 'b', 'c'] as Set)
    expect:
    constraint.validate('a')
    constraint.validate('b')
    constraint.validate('c')
    !constraint.validate('d')
  }

  def "string enum explanation"() {
    given:
    def desc = new EnumConstraint<String>(['alpha', 'beta'] as Set).describe("gamma")
    expect:
    desc.contains("gamma")
    desc.contains("alpha")
    desc.contains("beta")
  }

}
