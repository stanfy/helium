package com.stanfy.helium.entities.json

import com.stanfy.helium.Helium
import com.stanfy.helium.entities.TypedEntity
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import spock.lang.Specification

/**
 * Spec for GsonEntityWriter.
 */
class GsonEntityWriterSpec extends Specification {

  StringWriter out = new StringWriter()

  GsonEntityWriter writer

  def setup() {
    TypeResolver types = new Helium().defaultTypes().getProject().getTypes()
    writer = new GsonEntityWriter(out, types)
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

}
