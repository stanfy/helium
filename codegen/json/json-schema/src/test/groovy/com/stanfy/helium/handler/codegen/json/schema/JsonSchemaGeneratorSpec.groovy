package com.stanfy.helium.handler.codegen.json.schema

import com.stanfy.helium.DefaultType
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType
import com.stanfy.helium.model.constraints.EnumConstraint
import spock.lang.Specification

/**
 * Json scheme generation tests.
 *
 * @author Michael Pustovit mpustovit@stanfy.com.ua
 */
class JsonSchemaGeneratorSpec extends Specification {

  public static final String MESSAGE_NAME = "TestMessage"
  public static final String MESSAGE_DESCRIPTION = "Test description"

  private JsonSchemaGenerator generator
  private StringWriter writer

  def setup() {
    generator = new JsonSchemaGenerator(new File(""), new JsonSchemaGeneratorOptions())
    writer = new StringWriter()
  }

  def "generate schema for message without properties"() {
    setup:
    final Message msg = new Message()
    msg.setName(MESSAGE_NAME)
    msg.setDescription(MESSAGE_DESCRIPTION)

    when:
    generator.write(msg, writer)
    def schema = writer.toString()

    then:
    schema ==
        """{
  "\$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "description": "Test description"
}"""
  }

  def "should generate right scheme for message with properties"() {
    setup:
    final Message msg = new Message()
    msg.setName(MESSAGE_NAME)
    msg.setDescription(MESSAGE_DESCRIPTION)

    msg.addField(createField("field1", new Type(name: "string"), "Field1 description", false, true))
    msg.addField(createField("field2", new Type(name: DefaultType.DOUBLE.langName), null, false, false))
    msg.addField(createField("field3", new Type(name: "float"), "Field3 description", true, false))

    when:
    generator.write(msg, writer)
    def schema = writer.toString()

    then:
    schema ==
        """{
  "\$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "description": "Test description",
  "properties": {
    "field1": {
      "type": "string",
      "description": "Field1 description"
    },
    "field2": {
      "type": "number"
    }
  },
  "required": [
    "field1"
  ]
}"""
  }

  def "should generate right scheme for message with nested messages"() {
    setup:
    final Message msg = new Message()
    msg.setName(MESSAGE_NAME)
    msg.setDescription(MESSAGE_DESCRIPTION)

    msg.addField(createField("field1", new Type(name: "string"), "Field1 description", false, true))
    msg.addField(createField("field2", new Type(name: DefaultType.DOUBLE.langName), null, false, false))

    def nestedMsg = new Message()
    nestedMsg.setName("Nested message")
    nestedMsg.setDescription("Nested message description")
    nestedMsg.addField(createField("nestedField", new Type(name: "string"), "Nested field description", false, true))
    msg.addField(createField("field3", nestedMsg, "Field3 description", false, false))

    when:
    generator.write(msg, writer)
    def schema = writer.toString()

    then:
    schema ==
        """{
  "\$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "description": "Test description",
  "properties": {
    "field1": {
      "type": "string",
      "description": "Field1 description"
    },
    "field2": {
      "type": "number"
    },
    "field3": {
      "type": "object",
      "description": "Field3 description",
      "properties": {
        "nestedField": {
          "type": "string",
          "description": "Nested field description"
        }
      },
      "required": [
        "nestedField"
      ]
    }
  },
  "required": [
    "field1"
  ]
}"""
  }

  def "should generate right scheme for message with sequences"() {
    setup:
    final Message msg = new Message()
    msg.setName(MESSAGE_NAME)
    msg.setDescription(MESSAGE_DESCRIPTION)

    msg.addField(createField("field1", new Type(name: "string"), "Field1 description", false, true))
    msg.addField(createField("field2", new Type(name: DefaultType.DOUBLE.langName), null, false, false))

    def nestedMsg = new Message()
    nestedMsg.setName("Nested message")
    nestedMsg.setDescription("Nested message description")
    nestedMsg.addField(createField("nestedField", new Type(name: "string"), "Nested field description", false, true))

    def sequence = new Sequence()
    sequence.itemsType = nestedMsg
    sequence.setDescription("Sequence description")

    msg.addField(createField("field3", sequence, "Field3 description", false, false))

    when:
    generator.write(msg, writer)
    def schema = writer.toString()

    then:
    schema ==
        """{
  "\$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "description": "Test description",
  "properties": {
    "field1": {
      "type": "string",
      "description": "Field1 description"
    },
    "field2": {
      "type": "number"
    },
    "field3": {
      "type": "array",
      "description": "Field3 description",
      "items": {
        "type": "object",
        "description": "Nested message description",
        "properties": {
          "nestedField": {
            "type": "string",
            "description": "Nested field description"
          }
        },
        "required": [
          "nestedField"
        ]
      }
    }
  },
  "required": [
    "field1"
  ]
}"""
  }

  def "should translate enum constraint into enum"() {
    given:
    def msg = new Message(name: "MMM")
    Type str = new Type(name: "string")
    ConstrainedType enumStr = new ConstrainedType(str)
    enumStr.addConstraint(new EnumConstraint<String>(["master", "dev"] as Set))
    msg.addField(new Field(name: "branch", type: enumStr))
    generator.write(msg, writer)

    expect:
    writer.toString() == """{
  "\$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "branch": {
      "enum": [
        "master",
        "dev"
      ]
    }
  },
  "required": [
    "branch"
  ]
}"""
  }

  private static Field createField(String name, Type type, String description, boolean skip, boolean required) {
    def field = new Field()
    field.setName(name)
    field.setType(type)
    field.setDescription(description)
    field.setSkip(skip)
    field.required = required
    field
  }
}
