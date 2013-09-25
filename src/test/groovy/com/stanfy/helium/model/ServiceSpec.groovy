package com.stanfy.helium.model

import spock.lang.Specification

/**
 * Service spec.
 */
class ServiceSpec extends Specification {

  /** Service instance. */
  Service service = new Service()

  def "canonical name does not contain spaces"() {
    given:
    service.name = "Super API"
    expect:
    service.canonicalName == "SuperAPI"
  }

}
