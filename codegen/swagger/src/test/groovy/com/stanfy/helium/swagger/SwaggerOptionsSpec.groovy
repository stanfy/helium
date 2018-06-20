package com.stanfy.helium.swagger

import com.stanfy.helium.internal.utils.SelectionRules
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.Type
import spock.lang.Specification

/** Spec for SwaggerOptions. */
class SwaggerOptionsSpec extends Specification {

  private SwaggerOptions options

  def setup() {
    options = new SwaggerOptions()
  }


  def "check endpoints"() {
    given:
    ServiceMethod m1 = new ServiceMethod(type: MethodType.DELETE, path: '/path/one')
    ServiceMethod m2 = new ServiceMethod(type: MethodType.POST, path: '/path/two')
    ServiceMethod m3 = new ServiceMethod(type: MethodType.GET, path: '/path/three')

    when:
    options.endpoints.includes 'DELETE /path/.+', '.+/two.*'

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

  def "check types"() {
    given:
    options.types.includes('TestMessage')

    expect:
    options.checkIncludes(new Type(name: 'TestMessage'))
    !options.checkIncludes(new Type(name: 'AnotherName'))
  }

  def "check field names"() {
    given:
    Message msg = new Message(name: 'TestMessage')
    msg.addField(new Field(name: 'foo', type: new Type(name: 'string')))
    msg.addField(new Field(name: 'bar', type: new Type(name: 'string')))
    Message msg2 = new Message(name: 'TestMessage2')
    msg2.addField(new Field(name: 'foo', type: new Type(name: 'string')))
    msg2.addField(new Field(name: 'bar', type: new Type(name: 'string')))

    and:
    options.types.excludes('Something')
    def msgRules = new SelectionRules('TestMessage')
    msgRules.includes('\\w+')
    msgRules.excludes('bar')
    options.types.nest(msgRules)

    expect:
    options.checkIncludes(msg, msg.fields[0])
    !options.checkIncludes(msg, msg.fields[1])
    options.checkIncludes(msg2, msg2.fields[0])
  }

}
