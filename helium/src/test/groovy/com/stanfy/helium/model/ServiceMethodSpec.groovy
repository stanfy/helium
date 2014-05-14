package com.stanfy.helium.model

import spock.lang.Specification

/**
 * Spec for ServiceMethod.
 */
class ServiceMethodSpec extends Specification {

  ServiceMethod method = new ServiceMethod()

  def setup() {
    method.name = "test method"
  }

  def "canonical name is based on path and method type"() {
    when:
    method.name = "ababagalamaga"
    method.path = "/statuses/public.json"
    method.type = MethodType.DELETE

    then:
    method.canonicalName == "delete_statuses_public_json"
  }

  def "path should be prepended with '/'"() {
    given:
    method.path = "test"
    expect:
    method.path == '/test'
  }

  def "getPathWithParameters substitutes provided values"() {
    given:
    method.path = "/@param1/@param2/a/@param3/@param1-@param2/@encoded"

    expect:
    method.getPathWithParameters(
        param1: 'alpha',
        param2: 'beta',
        param3: 'Yulenka',
        encoded: 'encoded value'
    ) == "/alpha/beta/a/Yulenka/alpha-beta/encoded%20value"
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

  def "forms URI query with examples"() {
    given:
    Message testType = new Message(name: 'TestType')
    Type str = new Type(name: 'string')
    testType.addField(new Field(name : 'a', type: str, examples: ['1']))
    testType.addField(new Field(name : 'b', type: str, required: false, examples: ['2']))
    testType.addField(new Field(name : 'c', type: str, required: false))

    method.parameters = testType
    String q1 = method.getUriQueryWithExamples('UTF-8')

    method.parameters = null
    String q2 = method.getUriQueryWithExamples('UTF-8')

    testType.addField(new Field(name : 'd', required: true))
    method.parameters = testType
    String q3 = method.getUriQueryWithExamples('UTF-8')

    expect:
    q1 == '?a=1&b=2'
    q2 == ''
    q3 == ''

  }

  def "hasRequiredParameters should respect path parameters"() {
    given:
    method.path = "something/@param/test"
    expect:
    method.hasRequiredParameters()
  }

  def "hasRequiredParameters should respect required fields in params message"() {
    given:
    Message testType = new Message(name: 'TestType')
    testType.addField(new Field(name : 'a', type: new Type(name: 'string'), required: true))
    method.parameters = testType

    expect:
    method.hasRequiredParameters()
  }

  def "can return path parameter names"() {
    given:
    method.path = "something/@param/@two"
    expect:
    method.pathParameters == ['param', 'two']
  }

}
