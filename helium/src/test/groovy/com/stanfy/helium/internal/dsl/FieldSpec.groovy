package com.stanfy.helium.internal.dsl

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Type
import com.stanfy.helium.utils.ConfigurableProxy
import spock.lang.Specification

import static com.stanfy.helium.utils.DslUtils.runWithProxy

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
    runWithProxy(new ConfigurableProxy<Field>(field, new ProjectDsl())) {
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

  def "should allow any case"() {
    when:
    runWithProxy(new ConfigurableProxy<Field>(field, new ProjectDsl())) {
      name "Upper_Case_Id"
      description "First letter is in upper-case"
      type new Type(name : "long")
      required false
    }

    then:
    field.name == "Upper_Case_Id"
    field.description == "First letter is in upper-case"
    field.type.name == "long"
    !field.required
  }

}
