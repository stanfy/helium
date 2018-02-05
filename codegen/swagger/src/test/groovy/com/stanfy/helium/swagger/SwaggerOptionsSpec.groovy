package com.stanfy.helium.swagger

import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.ServiceMethod
import spock.lang.Specification

import java.util.regex.Pattern

/** Spec for SwaggerOptions. */
class SwaggerOptionsSpec extends Specification {

  private SwaggerOptions options

  def setup() {
    options = new SwaggerOptions()
  }

  def "use strings for patterns"() {
    when:
    options.includes '.*/some/path/.+', '/'

    then:
    options.includes.size() == 2
  }

  def "use string list for patterns"() {
    when:
    options.includes(['.*/some/path/.+', '/'])

    then:
    options.includes.size() == 2
  }

  def "use patterns"() {
    when:
    options.includePatterns([Pattern.compile('.*/some/path/.+')])

    then:
    options.includes.size() == 1
  }

  def "check includes"() {
    given:
    ServiceMethod m1 = new ServiceMethod(type: MethodType.DELETE, path: '/path/one')
    ServiceMethod m2 = new ServiceMethod(type: MethodType.POST, path: '/path/two')
    ServiceMethod m3 = new ServiceMethod(type: MethodType.GET, path: '/path/three')

    when:
    options.includes 'DELETE /path/.+', '.+/two.*'

    then:
    options.checkIncludes(m1)
    options.checkIncludes(m2)
    !options.checkIncludes(m3)
  }

  def "empty includes"() {
    given:
    ServiceMethod m1 = new ServiceMethod(type: MethodType.DELETE, path: '/path/one')
    ServiceMethod m2 = new ServiceMethod(type: MethodType.POST, path: '/path/two')
    ServiceMethod m3 = new ServiceMethod(type: MethodType.GET, path: '/path/three')

    expect:
    options.checkIncludes(m1)
    options.checkIncludes(m2)
    options.checkIncludes(m3)
  }
}
