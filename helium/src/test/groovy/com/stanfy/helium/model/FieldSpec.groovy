package com.stanfy.helium.model

import spock.lang.Specification

/**
 * Field specification.
 */
class FieldSpec extends Specification {

  /** Field instance. */
  Field field = new Field()

  def "setName should accept names with special characters"() {
    when:
    field.name = '$@tests'

    then:
    field.name == '$@tests'
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

  def "examples may be of any type"() {
    when:
    field.examples = [1, '2']
    then:
    field.examples == [1, '2']
  }

  def "getAlternatives is unmodifiable"() {
    when:
    field.alternatives = ['a']
    field.alternatives.add('b')

    then:
    thrown(UnsupportedOperationException)
    field.alternatives == ['a']
  }

}
