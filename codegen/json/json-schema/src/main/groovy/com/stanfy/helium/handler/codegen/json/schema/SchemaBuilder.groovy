package com.stanfy.helium.handler.codegen.json.schema

import com.stanfy.helium.DefaultType
import com.stanfy.helium.internal.utils.SelectionRules
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

  static final SelectionRules FULL_SELECTION = new SelectionRules("")

  private final String definitionsPrefix;

  SchemaBuilder() {
    this(null)
  }

  SchemaBuilder(String definitionsPrefix) {
    this.definitionsPrefix = definitionsPrefix
  }

  JsonType translateType(Type type) {
    if (type instanceof ConstrainedType) {
      if ((type as ConstrainedType).containsConstraint(EnumConstraint)) {
        return JsonType.ENUM
      }
      return translateType((type as ConstrainedType).baseType)
    }
    if (type instanceof FileType) {
      return JsonType.FILE
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
          return JsonType.STRING
        default:
          return JsonType.ANY
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

  JsonSchemaEntity makeSchemaFromMessage(Message msg, SelectionRules selection) {
    def schema = new JsonSchemaEntity()

    schema.type = JsonType.OBJECT
    schema.description = msg.getDescription()

    if (msg.fields) {
      SelectionRules msgSelection = selection.nested(msg.name)
      msg.activeFieldsWithParents.each { field ->
        if (msgSelection && !msgSelection.check(field.name)) {
          // Skip this field.
          return
        }

        if (field.sequence) {
          def sequence = new Sequence()
          sequence.itemsType = field.type
          sequence.anonymous = true
          field.type = sequence
        }

        def property = makeSchema(field.getType(), selection, true)
        if (field.description) {
          property.description = field.getDescription()
        }
        schema.addProperty(field.name, property)
      }

      msg.activeFieldsWithParents.grep { Field f -> f.required && (!msgSelection || msgSelection.check(f.name)) }.each {
        schema.addRequired(it.name)
      }
    }

    return schema
  }

  JsonSchemaEntity makeSchemaFromSequence(Sequence sequence, SelectionRules selection) {
    def schema = new JsonSchemaEntity()

    schema.type = JsonType.ARRAY
    schema.description = sequence.getDescription()
    schema.items = makeSchema(sequence.itemsType, selection, true)

    return schema
  }

  JsonSchemaEntity makeSchemaFromType(Type type) {
    return makeSchemaFromType(type, FULL_SELECTION)
  }

  JsonSchemaEntity makeSchemaFromType(Type type, SelectionRules selection) {
    println "makeSchemaFromType: " + type
    return makeSchema(type, selection, false)
  }

  private JsonSchemaEntity makeSchema(Type type, SelectionRules selection, boolean nested) {
    if (nested && definitionsPrefix && !type.primitive && !type.anonymous) {
      return new JsonSchemaEntity("$definitionsPrefix$type.name")
    }

    def property

    def jsonType = translateType(type)

    if (jsonType == JsonType.ANY) {
      return new JsonSchemaEntity()
    }

    switch (jsonType) {
      case JsonType.OBJECT:
        property = (type instanceof Dictionary
            ? makeSchemaFromDict((Dictionary) type)
            : makeSchemaFromMessage((Message) type, selection))
        break
      case JsonType.ARRAY:
        property = makeSchemaFromSequence((Sequence) type, selection)
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
