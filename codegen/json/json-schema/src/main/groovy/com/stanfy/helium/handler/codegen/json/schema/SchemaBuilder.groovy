package com.stanfy.helium.handler.codegen.json.schema

import com.stanfy.helium.DefaultType
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType
import com.stanfy.helium.model.constraints.EnumConstraint
import groovy.transform.CompileStatic

@CompileStatic
class SchemaBuilder {

  JsonType translateType(Type type) {
    if (type instanceof ConstrainedType) {
      if (type.containsConstraint(EnumConstraint)) {
        return JsonType.ENUM
      }
      return translateType(type.baseType)
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
      if (type instanceof Message) {
        return JsonType.OBJECT
      }

      if (type instanceof Sequence) {
        return JsonType.ARRAY
      }
    }

    throw new IllegalArgumentException("'${type.name}' simple type isn't supported")
  }

  JsonSchemaEntity makeSchemaFromMessage(Message msg) {
    def schema = new JsonSchemaEntity()

    schema.type = JsonType.OBJECT
    schema.description = msg.getDescription()

    if (msg.fields) {
      msg.activeFields.each { field ->
        def property = makeSchemaFromType(field.getType())
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
    schema.items = makeSchemaFromType(sequence.itemsType)

    return schema
  }

  JsonSchemaEntity makeSchemaFromType(Type type) {
    def property

    def jsonType = translateType(type)

    switch (jsonType) {
      case JsonType.OBJECT:
        property = makeSchemaFromMessage((Message) type)
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
