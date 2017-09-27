package com.stanfy.helium.handler.codegen.json.schema

import com.stanfy.helium.DefaultType
import com.stanfy.helium.model.Dictionary
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.FileType
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType
import com.stanfy.helium.model.constraints.EnumConstraint
import groovy.transform.CompileStatic

@CompileStatic
class SchemaBuilder {

  private final String definitionsPrefix;

  SchemaBuilder() {
    this(null)
  }

  SchemaBuilder(String definitionsPrefix) {
    this.definitionsPrefix = definitionsPrefix
  }

  JsonType translateType(Type type) {
    if (type instanceof ConstrainedType) {
      if (type.containsConstraint(EnumConstraint)) {
        return JsonType.ENUM
      }
      return translateType(type.baseType)
    }
    if (type instanceof FileType) {
      return JsonType.FILE;
    }
    if (type.isPrimitive()) {
      switch (type.name) {
        case DefaultType.DOUBLE.langName:
        case DefaultType.FLOAT.langName:
          return JsonType.NUMBER
        case DefaultType.INT32.langName:
        case DefaultType.INT64.langName:
          return JsonType.INTEGER
        case DefaultType.BOOL.langName:
          return JsonType.BOOLEAN
        case DefaultType.STRING.langName:
        case DefaultType.BYTES.langName:
        default:
          return JsonType.STRING
      }
    } else {
      if (type instanceof Message || type instanceof Dictionary) {
        return JsonType.OBJECT
      }

      if (type instanceof Sequence) {
        return JsonType.ARRAY
      }
    }

    throw new IllegalArgumentException("'${type.name}' simple type isn't supported")
  }

  JsonSchemaEntity makeSchemaFromDict(Dictionary dict) {
    def schema = new JsonSchemaEntity()
    schema.type = JsonType.OBJECT
    return schema
  }

  JsonSchemaEntity makeSchemaFromMessage(Message msg) {
    def schema = new JsonSchemaEntity()

    schema.type = JsonType.OBJECT
    schema.description = msg.getDescription()

    if (msg.fields) {
      msg.activeFields.each { field ->
        def property = makeSchema(field.getType(), true)
        if (field.description) {
          property.description = field.getDescription()
        }
        schema.addProperty(field.name, property)
      }

      msg.fields.grep { Field f -> f.required }.each { schema.addRequired(it.name) }
    }

    return schema
  }

  JsonSchemaEntity makeSchemaFromSequence(Sequence sequence) {
    def schema = new JsonSchemaEntity()

    schema.type = JsonType.ARRAY
    schema.description = sequence.getDescription()
    schema.items = makeSchema(sequence.itemsType, true)

    return schema
  }

  JsonSchemaEntity makeSchemaFromType(Type type) {
    return makeSchema(type, false)
  }

  private JsonSchemaEntity makeSchema(Type type, boolean nested) {
    if (nested && definitionsPrefix && !type.primitive && !type.anonymous) {
      return new JsonSchemaEntity("$definitionsPrefix$type.name")
    }

    def property

    def jsonType = translateType(type)

    switch (jsonType) {
      case JsonType.OBJECT:
        property = (type instanceof Dictionary
            ? makeSchemaFromDict((Dictionary) type)
            : makeSchemaFromMessage((Message) type))
        break
      case JsonType.ARRAY:
        property = makeSchemaFromSequence((Sequence) type)
        break
      case JsonType.ENUM:
        property = new JsonSchemaEntity()
        EnumConstraint<String> constraint = ((ConstrainedType) type)
            .getConstraint(EnumConstraint.class) as EnumConstraint<String>
        property.enumeration = new ArrayList<>(constraint.values)
        break
      default:
        property = new JsonSchemaEntity()
        property.type = jsonType
    }

    if (!property.@description) {
      property.@description = type.description
    }
    return property
  }

}
