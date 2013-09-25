package com.stanfy.helium.model

import spock.lang.Specification

/**
 * Spec for ServiceMethod.
 */
class ServiceMethodSpec extends Specification {

  ServiceMethod method = new ServiceMethod()

  def "canonical name is based on path"() {
    when:
    method.path = "/statuses/public.json"

    then:
    method.canonicalName == "statuses_public_json"
  }

}
