package com.stanfy.helium.internal.entities

import com.stanfy.helium.model.Dictionary
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Spec for TypesEntityBuilder.
 */
class TypedEntityValueBuilderSpec extends Specification {

  def "uses plain object for primitives"() {
    when:
    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(new Type(name: 'int'))
    def value = builder.from(2)

    then:
    value == 2
  }

  def "does not allow plain object for messages"() {
    when:
    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(new Message(name: 'Msg'))
    builder.from(2)

    then:
    thrown(IllegalArgumentException)
  }

  def "builds messages with closures"() {
    when:
    Message msg = new Message(name: 'Msg')
    msg.addField(new Field(name: 'f1', type: new Type(name: 'string')))
    msg.addField(new Field(name: 'f2', type: new Type(name: 'int')))
    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(msg)
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
    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(msg)
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

    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(msgParent)
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

    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(msg)
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

    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(msgParent)
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
    def value = new TypedEntityValueBuilder(seq).from([1, 2])
    then:
    value == [1, 2]
  }

  def "works with root sequences"() {
    when:
    Message msgChild = new Message(name: 'MsgChild')
    msgChild.addField(new Field(name: 'f1', type: new Type(name: 'string')))
    Sequence seq = new Sequence(name: 'Seq', itemsType: msgChild)
    def value = new TypedEntityValueBuilder(seq).from([
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

  def "build dictionaries"() {
    when:
    Message valueMessage = new Message(name: 'value-message')
    valueMessage.addField(new Field(name: 'someField', type: new Type(name: 'string')))
    Dictionary dictionary = new Dictionary(name: 'Dict', key: new Type(name: 'string'), value: valueMessage)
    def value = new TypedEntityValueBuilder(dictionary).from {
      '5' {
        someField 'value 1'
      }
      '7' {
        someField 'value 2'
      }
    }
    then:
    value['5'] instanceof Map
    value['5'].someField == 'value 1'
    value['7'].someField == 'value 2'
  }

  def "complex dictionary entries"() {
    when:
    Message complexType = new Message(name: 'ComplexType')
    complexType.addField(new Field(name: 'f1', type: new Type(name: 'string')))
    Dictionary dictionary = new Dictionary(name: 'Dict', key: complexType, value: complexType)
    def value = new TypedEntityValueBuilder(dictionary).from {
      entry(
          {f1 'key'},
          {f1 'value'}
      )
    }

    then:
    !value.keySet().empty
    !value.values().empty
    value.keySet().first() instanceof Map
    value.keySet().first()['f1'] == 'key'
    value.values().first()['f1'] == 'value'
    value[['f1': 'key']] == ['f1': 'value']
  }

  def "fields of parent message"() {
    when:
    Message base = new Message(name: 'Base')
    base.addField(new Field(name: 'baseField', type: new Type(name: 'string')))
    Message msg = new Message(name: 'Msg', parent: base)
    msg.addField(new Field(name: 'mainField', type: new Type(name: 'int32')))
    def value = new TypedEntityValueBuilder(msg).from {
      mainField 42
      baseField 'forty two'
    }

    then:
    value.mainField == 42
    value.baseField == 'forty two'
  }

  def "custom defined types"() {
    when:
    Type customType = new Type(name: 'schemaLessMessage', description: 'some custom type')
    def value = new TypedEntityValueBuilder(customType).from {
      field1 'value1'
      field2 123
      field3 (['1', '2'])
    }

    then:
    value.field1 == 'value1'
    value.field2 == 123
    value.field3 == ['1', '2']
  }

  def "no closures in custom values"() {
    when:
    Type customType = new Type(name: 'schemaLessMessage', description: 'some custom type')
    def value = new TypedEntityValueBuilder(customType).from {
      fieldName { something 'else' }
    }

    then:
    def e = thrown(IllegalArgumentException)
    e.message.contains('schemaLessMessage')
    e.message.contains('fieldName')
  }

}
