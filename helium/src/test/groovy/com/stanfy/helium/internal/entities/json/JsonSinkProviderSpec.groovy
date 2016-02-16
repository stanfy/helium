package com.stanfy.helium.internal.entities.json

import com.squareup.okhttp.MediaType
import com.stanfy.helium.Helium
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.internal.entities.EntitiesSink
import com.stanfy.helium.internal.entities.TypedEntity
import com.stanfy.helium.internal.entities.TypedEntityValueBuilder
import com.stanfy.helium.model.Dictionary
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import okio.Buffer
import spock.lang.Specification

import java.nio.charset.Charset

/**
 * Spec for JSON sink.
 */
class JsonSinkProviderSpec extends Specification {

  Buffer out

  ProjectDsl dsl

  EntitiesSink writer

  def setup() {
    out = new Buffer()
    dsl = new Helium().defaultTypes().getProject() as ProjectDsl
    TypeResolver types = dsl.getTypes()
    writer = new JsonSinkProvider()
        .create(out, Charset.forName("UTF-8"), types.findConverters(MediaType.parse("*/json")))
  }

  def "can write primitives"() {
    when:
    writer.write(new TypedEntity(new Type(name: "int32"), 2))
    writer.write(new TypedEntity(new Type(name: "string"), ' - '))
    writer.write(new TypedEntity(new Type(name: "bool"), false))
    then:
    out.readUtf8() == '2" - "false'
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
    m.addField(new Field(name: 'f3', type: new Type(name: 'bool'), sequence: true))
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

}
