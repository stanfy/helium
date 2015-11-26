package com.stanfy.helium.internal.entities.json

import com.stanfy.helium.Helium
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.internal.entities.TypedEntity
import com.stanfy.helium.internal.entities.TypedEntityValueBuilder
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import spock.lang.Specification

/**
 * Spec for JsonEntityWriter.
 */
class JsonEntityWriterSpec extends Specification {

  StringWriter out = new StringWriter()

  ProjectDsl dsl

  JsonEntityWriter writer

  def setup() {
    dsl = new Helium().defaultTypes().getProject() as ProjectDsl
    TypeResolver types = dsl.getTypes()
    writer = new JsonEntityWriter(out, types.findConverters("json"))
  }

  def "can write primitives"() {
    when:
    writer.write(new TypedEntity(new Type(name: "int32"), 2))
    writer.write(new TypedEntity(new Type(name: "string"), ' - '))
    writer.write(new TypedEntity(new Type(name: "bool"), false))
    then:
    out.toString() == '2" - "false'
  }

  def "can write messages"() {
    when:
    Message m = new Message(name: 'Msg')
    m.addField(new Field(name: 'f1', type: new Type(name: 'int32')))
    m.addField(new Field(name: 'f2', type: new Type(name: 'bool')))
    writer.write(new TypedEntity(m, [f1: 2, f2: true]))

    then:
    out.toString() == '{"f1":2,"f2":true}'
  }

  def "can write sequences"() {
    when:
    Message m = new Message(name: 'Msg')
    m.addField(new Field(name: 'f1', type: new Type(name: 'int32')))
    m.addField(new Field(name: 'f2', type: new Type(name: 'bool'), sequence: true))
    writer.write(new TypedEntity(m, [f1: 2, f2: [true, false]]))

    then:
    out.toString() == '{"f1":2,"f2":[true,false]}'
  }

  def "skips sequences when they are null"() {
    when:
    Message m = new Message(name: 'Msg')
    m.addField(new Field(name: 'f1', type: new Type(name: 'int32')))
    m.addField(new Field(name: 'f2', type: new Type(name: 'bool'), sequence: true))
    m.addField(new Field(name: 'f3', type: new Type(name: 'bool'), sequence: true))
    writer.write(new TypedEntity(m, [f1: 2, f2: [true, false]]))

    then:
    out.toString() == '{"f1":2,"f2":[true,false]}'
  }

  def "can write some complex messages"() {
    given:
    dsl.type 'SomeMessage' message {
      id(type: long, required: true, examples: ['1'])
      name(type: 'string', required: true, examples: ['My List'])
      email(type: 'string', required: false, examples: ['myfriend@test.com'])
    }
    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(dsl.types.byName('SomeMessage'))
    def msg = builder.from {
      id 321
      name 'some name'
    }
    writer.write(new TypedEntity(dsl.types.byName('SomeMessage'), msg))

    expect:
    out.toString() == '{"id":321,"name":"some name"}'
  }

  def "can write dates"() {
    given:
    dsl.type "foo" spec {
      description "bar"
      to("json") { asDate("yyyy-MM-dd") }
    }
    dsl.type "FooMsg" message {
      bar 'foo'
    }
    writer.write(new TypedEntity<Type>(dsl.types.byName('FooMsg'), ['bar' : Date.parse("yyyy-MM-dd", "2013-07-11")]))

    expect:
    out.toString() == '{"bar":"2013-07-11"}'
  }

  def "does not write skipped fields"() {
    given:
    dsl.type 'SomeMessage' message {
      id(type: long, required: true, examples: ['1'])
      name(skip: true)
    }
    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(dsl.types.byName('SomeMessage'))
    def msg = builder.from {
      id 321
      name 'some name'
    }
    writer.write(new TypedEntity(dsl.types.byName('SomeMessage'), msg))

    expect:
    out.toString() == '{"id":321}'
  }

}
