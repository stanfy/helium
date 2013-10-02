package com.stanfy.helium.entities

import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Spec for TypesEntityBuilder.
 */
class TypedEntityBuilderSpec extends Specification {

  def "uses plain object for primitives"() {
    when:
    TypedEntityBuilder builder = new TypedEntityBuilder(new Type(name: 'int'))
    def value = builder.from(2)

    then:
    value == 2
  }

  def "does not allow plain object for messages"() {
    when:
    TypedEntityBuilder builder = new TypedEntityBuilder(new Message(name: 'Msg'))
    builder.from(2)

    then:
    thrown(IllegalArgumentException)
  }

  def "builds messages with closures"() {
    when:
    Message msg = new Message(name: 'Msg')
    msg.addField(new Field(name: 'f1', type: new Type(name: 'string')))
    msg.addField(new Field(name: 'f2', type: new Type(name: 'int')))
    TypedEntityBuilder builder = new TypedEntityBuilder(msg)
    def entity = builder.from {
      f1 'value'
      f2 3
    }

    then:
    entity.f1 == 'value'
    entity.f2 == 3
  }

  def "checks message fields"() {
    when:
    Message msg = new Message(name: 'Msg')
    msg.addField(new Field(name: 'f1', type: new Type(name: 'string')))
    TypedEntityBuilder builder = new TypedEntityBuilder(msg)
    builder.from {
      f1 'value'
      f2 3
    }

    then:
    def e = thrown(IllegalArgumentException)
    e.message.contains("f2")
  }

  def "recursively builds messages"() {
    when:
    Message msgChild = new Message(name: 'MsgChild')
    msgChild.addField(new Field(name: 'f1', type: new Type(name: 'string')))
    Message msgParent = new Message(name: 'MsgParent')
    msgParent.addField(new Field(name: 'child', type: msgChild))

    TypedEntityBuilder builder = new TypedEntityBuilder(msgParent)
    def value = builder.from {
      child {
        f1 'value'
      }
    }

    then:
    value.child.f1 == 'value'
  }

  def "works with primitive sequence fields"() {
    when:
    Message msg = new Message(name: 'Msg')
    msg.addField(new Field(name: 'list', type: new Type(name: 'string'), sequence: true))

    TypedEntityBuilder builder = new TypedEntityBuilder(msg)
    def value = builder.from {
      list (['a', 'b'])
    }

    then:
    value.list == ['a', 'b']
  }

  def "works with message sequence fields"() {
    when:
    Message msgChild = new Message(name: 'MsgChild')
    msgChild.addField(new Field(name: 'f1', type: new Type(name: 'string')))
    Message msgParent = new Message(name: 'MsgParent')
    msgParent.addField(new Field(name: 'child', type: msgChild, sequence: true))

    TypedEntityBuilder builder = new TypedEntityBuilder(msgParent)
    def value = builder.from {
      child ([
          {
            f1 'a'
          },
          {
            f1 'b'
          }
      ])
    }

    then:
    value.child[0].f1 == 'a'
    value.child[1].f1 == 'b'
  }

  def "works with root primitive sequences"() {
    when:
    Sequence seq = new Sequence(name: 'Seq', itemsType: new Type(name: 'foo'))
    def value = new TypedEntityBuilder(seq).from([1, 2])
    then:
    value == [1, 2]
  }

  def "works with root sequences"() {
    when:
    Message msgChild = new Message(name: 'MsgChild')
    msgChild.addField(new Field(name: 'f1', type: new Type(name: 'string')))
    Sequence seq = new Sequence(name: 'Seq', itemsType: msgChild)
    def value = new TypedEntityBuilder(seq).from([
        {
          f1 'a'
        },
        {
          f1 'b'
        }
    ])

    then:
    value[0].f1 == 'a'
    value[1].f1 == 'b'
  }

}
