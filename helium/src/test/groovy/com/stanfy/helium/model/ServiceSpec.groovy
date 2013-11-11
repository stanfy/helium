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
    service.canonicalName == "Super_API"
  }

  def "can generate service method uri"() {
    given:
    service.location = "http://api.com/"
    ServiceMethod m = new ServiceMethod(path : '/person/show')

    expect:
    service.getMethodUri(null, m) == "http://api.com/person/show"
  }

  def "substitutes request method path parameters"() {
    given:
    service.location = "http://api.com/"
    ServiceMethod m = new ServiceMethod(path : '/person/show/@id')
    m.testInfo.useExamples = true
    m.testInfo.pathExample = [id:'123']

    expect:
    service.getMethodUri(m.testInfo, m) == "http://api.com/person/show/123"
  }

}
