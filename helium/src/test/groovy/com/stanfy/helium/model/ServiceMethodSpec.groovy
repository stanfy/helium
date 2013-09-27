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

  def "getPathWithParameters substitutes provided values"() {
    given:
    method.path = "/@param1/@param2/a/@param3/@param1-@param2"

    expect:
    method.getPathWithParameters(param1: 'alpha', param2: 'beta', param3: 'Yulenka') == "/alpha/beta/a/Yulenka/alpha-beta"
  }

  def "hasParametrizedPath checks for @"() {
    given:
    method.path = "@param"
    boolean v1 = method.hasParametrizedPath()
    method.path = "param"
    boolean v2 = method.hasParametrizedPath()

    expect:
    v1
    !v2
  }

}
