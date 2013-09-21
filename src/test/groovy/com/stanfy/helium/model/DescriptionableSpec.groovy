package com.stanfy.helium.model

import com.stanfy.helium.dsl.ConfigurableProxy
import com.stanfy.helium.dsl.Dsl
import spock.lang.Specification

/**
 * Spec for Service.
 */
class DescriptionableSpec extends Specification {

  /** Service instance. */
  Descriptionable service = new Descriptionable()

  def "should be configurable"() {
    when:
    new ConfigurableProxy<Descriptionable>(service, new Dsl()).configure {
      name "abc"
      description "hey"
    }

    then:
    service.name == "abc"
    service.description == "hey"
  }

}
