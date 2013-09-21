package com.stanfy.helium.model

import com.stanfy.helium.dsl.ConfigurableProxy
import com.stanfy.helium.dsl.Dsl
import spock.lang.Specification

/**
 * Spec for Type.
 */
class TypeSpec extends Specification {

  /** Type under the test. */
  Type type = new Type()

  def "should be configurable"() {
    when:
    new ConfigurableProxy<Type>(type, new Dsl()).configure {
      name "Int64"
      description "Java long"
    }

    then:
    type.name == "Int64"
    type.description == "Java long"
  }

}
