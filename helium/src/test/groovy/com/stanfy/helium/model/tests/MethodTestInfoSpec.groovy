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

}
