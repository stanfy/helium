package com.stanfy.helium.model

import spock.lang.Specification

/**
 * Field specification.
 */
class FieldSpec extends Specification {

  /** Field instance. */
  Field field = new Field()

  def "setName should throw when bad name"() {
    when:
    field.name = '$'

    then:
    def e = thrown(IllegalArgumentException)
    e.message.startsWith("Name must match")
  }

  def "setName should accept good names"() {
    when:
    field.name = 'a'

    then:
    field.name == 'a'
  }

  def "setExamples checks type"() {
    when:
    field.type = new Message(name : 'A')
    field.examples = ['a']

    then:
    def e = thrown(IllegalStateException)
    e.message.startsWith("Example")
  }

  def "getExamples is unmodifiable"() {
    when:
    field.examples = ['a']
    field.examples.add('b')

    then:
    thrown(UnsupportedOperationException)
    field.examples == ['a']
  }

}
