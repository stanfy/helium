package com.stanfy.helium.format.json

import com.google.gson.stream.JsonWriter
import com.squareup.okhttp.MediaType
import com.stanfy.helium.Helium
import com.stanfy.helium.format.PrimitiveWriter
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.internal.entities.EntitiesSink
import com.stanfy.helium.internal.entities.TypedEntity
import com.stanfy.helium.internal.entities.TypedEntityValueBuilder
import com.stanfy.helium.model.Dictionary
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import okio.Buffer
import spock.lang.Specification

/**
 * Spec for JSON sink.
 */
class JsonWriteFormatSpec extends Specification {

  Buffer out

  ProjectDsl dsl

  EntitiesSink writer

  def setup() {
    out = new Buffer()
    dsl = new Helium().defaultTypes().getProject() as ProjectDsl
    writer = new EntitiesSink.Builder()
        .into(out)
        .mediaType(MediaType.parse("application/json"))
        .provider(JsonFormatProvider.Writer.class)
        .customAdapter(new Type(name: 'foo'), { JsonWriter w, def type, Date value ->
          w.value(value.format('yyyy-MM-dd'))
        } as PrimitiveWriter<JsonWriter>)
        .build()
  }

  def "can write primitive int"() {
    when:
    writer.write(new TypedEntity(new Type(name: "int32"), 2))
    then:
    out.readUtf8() == '2'
  }

  def "can write primitive string"() {
    when:
    writer.write(new TypedEntity(new Type(name: "string"), ' - '))
    then:
    out.readUtf8() == '" - "'
  }

  def "can write primitive bool"() {
    when:
    writer.write(new TypedEntity(new Type(name: "bool"), false))
    then:
    out.readUtf8() == 'false'
  }

  def "can write messages"() {
    when:
    Message m = new Message(name: 'Msg')
    m.addField(new Field(name: 'f1', type: new Type(name: 'int32')))
    m.addField(new Field(name: 'f2', type: new Type(name: 'bool')))
    writer.write(new TypedEntity(m, [f1: 2, f2: true]))

    then:
    out.readUtf8() == '{"f1":2,"f2":true}'
  }

  def "can write sequences"() {
    when:
    Message m = new Message(name: 'Msg')
    m.addField(new Field(name: 'f1', type: new Type(name: 'int32')))
    m.addField(new Field(name: 'f2', type: new Type(name: 'bool'), sequence: true))
    writer.write(new TypedEntity(m, [f1: 2, f2: [true, false]]))

    then:
    out.readUtf8() == '{"f1":2,"f2":[true,false]}'
  }

  def "skips sequences when they are null"() {
    when:
    Message m = new Message(name: 'Msg')
    m.addField(new Field(name: 'f1', type: new Type(name: 'int32')))
    m.addField(new Field(name: 'f2', type: new Type(name: 'bool'), sequence: true))
    m.addField(new Field(name: 'f3', type: new Type(name: 'bool'), sequence: true, required: false))
    writer.write(new TypedEntity(m, [f1: 2, f2: [true, false]]))

    then:
    out.readUtf8() == '{"f1":2,"f2":[true,false]}'
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
    out.readUtf8() == '{"id":321,"name":"some name"}'
  }

  def "supports custom adapters"() {
    given:
    dsl.type "foo" spec {
      description "bar"
    }
    dsl.type "FooMsg" message {
      bar 'foo'
    }
    writer.write(new TypedEntity<Type>(dsl.types.byName('FooMsg'), ['bar' : Date.parse("yyyy-MM-dd", "2013-07-11")]))

    expect:
    out.readUtf8() == '{"bar":"2013-07-11"}'
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
    out.readUtf8() == '{"id":321}'
  }

  def "writes dictionaries"() {
    given:
    Dictionary dict = new Dictionary(name: 'TestDict', key: new Type(name: 'string'), value: new Type(name: 'int32'))
    TypedEntityValueBuilder builder = new TypedEntityValueBuilder(dict)
    def value = builder.from {
      'a' 1
      'b' 2
      'c' 3
    }
    writer.write(new TypedEntity<Type>(dict, value))

    expect:
    out.readUtf8() == '{"a":1,"b":2,"c":3}'
  }

  def "write parent fields"() {
    given:
    Message base = new Message(name: 'Base')
    base.addField(new Field(name: 'baseField', type: new Type(name: 'string')))
    Message msg = new Message(name: 'Msg', parent: base)
    msg.addField(new Field(name: 'mainField', type: new Type(name: 'int32')))
    def value = new TypedEntityValueBuilder(msg).from {
      mainField 42
      baseField 'forty two'
    }
    writer.write(new TypedEntity<Type>(msg, value))

    expect:
    out.readUtf8() == '{"mainField":42,"baseField":"forty two"}'
  }

}
