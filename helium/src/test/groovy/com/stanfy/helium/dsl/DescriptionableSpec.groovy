package com.stanfy.helium.dsl

import com.stanfy.helium.model.Descriptionable
import spock.lang.Specification

/**
 * Spec for Service.
 */
class DescriptionableSpec extends Specification {

  /** Service instance. */
  Descriptionable service = new Descriptionable()

  def "should be configurable"() {
    when:
    ProjectDsl.callConfigurationSpec(new ConfigurableProxy<Descriptionable>(service, new ProjectDsl())) {
      name "abc"
      description "hey"
    }

    then:
    service.name == "abc"
    service.description == "hey"
  }

}
