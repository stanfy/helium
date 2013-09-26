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

  def "can generate service method uri"() {
    given:
    service.location = "http://api.com"
    ServiceMethod m = new ServiceMethod(path : 'person/show')

    expect:
    service.getMethodUri(m) == "http://api.com/person/show"
  }

}
