package com.stanfy.helium.dsl

import com.stanfy.helium.model.Type
import com.stanfy.helium.utils.ConfigurableProxy
import spock.lang.Specification

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Spec for Type.
 */
class TypeSpec extends Specification {

  /** Type under the test. */
  Type type = new Type()

  def "should be configurable"() {
    when:
    runWithProxy(new ConfigurableProxy<Type>(type, new ProjectDsl())) {
      name "Int64"
      description "Java long"
    }

    then:
    type.name == "Int64"
    type.description == "Java long"
  }

}
