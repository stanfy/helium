package com.stanfy.helium.model

import spock.lang.Specification

/**
 * Spec for field.
 */
class FieldSpec extends Specification {

  /** Field under the test. */
  Field field = new Field()

  def "should be required by default"() {
    expect:
    field.required
  }

  def "should be configurable"() {
    when:
    field.configure {
      name "id"
      description "Identifier"
      type new Type(name : "long")
      required false
    }

    then:
    field.name == "id"
    field.description == "Identifier"
    field.type.name == "long"
    !field.required
  }

}
