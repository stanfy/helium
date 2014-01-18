package com.stanfy.helium.model

import spock.lang.Specification

/**
 * Tests for Message.
 */
class MessageSpec extends Specification {

  def Message message
  def Type type

  def setup() {
    message = new Message(name: 'Test')
    type = new Type(name: 'test')
  }

  def "required fields should not include skipped ones"() {
    given:
    message.addField(new Field(name: 'a', type: type))
    message.addField(new Field(name: 'b', type: type, skip: true))
    message.addField(new Field(name: 'c', type: type, skip: true))

    expect:
    message.requiredFields.size() == 1
    message.requiredFields[0].name == 'a'
  }

  def "active fields do not include skipped ones"() {
    given:
    message.addField(new Field(name: 'a', type: type))
    message.addField(new Field(name: 'b', type: type, skip: true))
    message.addField(new Field(name: 'c', type: type, skip: true))
    message.addField(new Field(name: 'd', type: type, required: false))

    expect:
    message.activeFields.collect { it.name } == ['a', 'd']
  }

}
