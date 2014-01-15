package com.stanfy.helium.model

import spock.lang.Specification

/**
 * Tests for Message.
 */
class MessageSpec extends Specification {

  def "required fields should not include skipped ones"() {
    given:
    Message m = new Message(name: 'Test')
    def type = new Type(name: 'test')
    m.addField(new Field(name: 'a', type: type))
    m.addField(new Field(name: 'b', type: type, skip: true))
    m.addField(new Field(name: 'c', type: type, skip: true))

    expect:
    m.requiredFields.size() == 1
    m.requiredFields[0].name == 'a'
  }

}
