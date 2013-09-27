package com.stanfy.helium.model.tests

import spock.lang.Specification

/**
 * Spec for MethodTestInfo.
 */
class MethodTestInfoSpec extends Specification {

  MethodTestInfo info = new MethodTestInfo()

  def "resolve() uses global value if local is not defined"() {
    when:
    TestsInfo global = new TestsInfo(useExamples: true)
    info.useExamples = false

    then:
    new MethodTestInfo().resolve(global).useExamples
    !info.resolve(global).useExamples
  }

  def "resolve() merges headers map"() {
    when:
    TestsInfo global = new TestsInfo(httpHeaders: ['1' : 'a', '2' : 'b'])
    info.httpHeaders = ['1' : 'c', '3' : 'd']

    then:
    info.resolve(global).httpHeaders == [
        '1' : 'c',
        '2' : 'b',
        '3' : 'd'
    ]
  }

}
