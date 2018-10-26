package com.stanfy.helium.handler.codegen.json.schema

import com.stanfy.helium.DefaultType
import com.stanfy.helium.internal.utils.SelectionRules
import com.stanfy.helium.model.Dictionary
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import spock.lang.Specification

class SchemaBuilderSpec extends Specification {

  private SchemaBuilder builder

  void setup() {
    builder = new SchemaBuilder()
  }

  def "should translate Helium data type into the correspondent JSON schema data type"() {
    expect:
    builder.translateType(new Type(name: heliumType.getLangName())) == jsonType

    where:
    heliumType         | jsonType
    DefaultType.BOOL   | JsonType.BOOLEAN
    DefaultType.BYTES  | JsonType.STRING
    DefaultType.DOUBLE | JsonType.NUMBER
    DefaultType.FLOAT  | JsonType.NUMBER
    DefaultType.INT32  | JsonType.INTEGER
    DefaultType.INT64  | JsonType.INTEGER
    DefaultType.STRING | JsonType.STRING
  }

  def "should translate complex types into object"() {
    setup:
    def msg = new Message(name: "ComplexType")
    def list = new Sequence()
    def dict = new Dictionary(key: new Type(name: 't1'), value: new Type(name: 't2'))

    expect:
    builder.translateType(msg) == JsonType.OBJECT
    builder.translateType(list) != JsonType.OBJECT
    builder.translateType(dict) == JsonType.OBJECT
  }

  def "custom types"() {
    setup:
    def customType = new Type(name: 'customType')

    expect:
    builder.translateType(customType) == JsonType.ANY
  }

  def "should translate sequences into arrays"() {
    setup:
    def list = new Sequence()
    def msg = new Message(name: "ComplexType")

    expect:
    builder.translateType(list) == JsonType.ARRAY
    builder.translateType(msg) != JsonType.ARRAY
  }

  def "should propagate type descriptions"() {
    given:
    def type = new Type(name: "double", description: "bla bla")
    expect:
    builder.makeSchemaFromType(type).description == "bla bla"
  }

  def "nested types can be expressed as references"() {
    given:
    builder = new SchemaBuilder("#/definitions/")
    def msg = new Message(name: 'Complex'), nestedMessage = new Message(name: 'AnotherComplex')
    msg.addField(new Field(name: 'ff', type: nestedMessage))

    expect:
    builder.makeSchemaFromType(msg).@properties.ff?.ref == '#/definitions/AnotherComplex'
    builder.makeSchemaFromType(msg).@properties.ff?.type == null
  }

  def "use selection to filter fields"() {
    given:
    def msg = new Message(name: "ComplexType")
    msg.addField(new Field(name: 'foo', type: new Type(name: 'string')))
    msg.addField(new Field(name: 'bar', type: new Type(name: 'string')))
    def msgSelection = new SelectionRules(msg.name)
    msgSelection.excludes('foo')
    def selection = new SelectionRules("test")
    selection.nest(msgSelection)

    when:
    def schema = builder.makeSchemaFromType(msg, selection)

    then:
    schema.@properties.keySet() == ['bar'] as Set
    schema.required == ['bar']
  }
}
