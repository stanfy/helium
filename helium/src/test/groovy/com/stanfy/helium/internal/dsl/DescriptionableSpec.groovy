package com.stanfy.helium.internal.dsl

import com.stanfy.helium.model.Descriptionable
import com.stanfy.helium.internal.utils.ConfigurableProxy
import spock.lang.Specification

import static com.stanfy.helium.internal.utils.DslUtils.runWithProxy

/**
 * Spec for Service.
 */
class DescriptionableSpec extends Specification {

  /** Service instance. */
  Descriptionable service = new Descriptionable()

  def "should be configurable"() {
    when:
    runWithProxy(new ConfigurableProxy<Descriptionable>(service, new ProjectDsl())) {
      name "abc"
      description "hey"
    }

    then:
    service.name == "abc"
    service.description == "hey"
  }

}
