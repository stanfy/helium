package com.stanfy.helium.handler.codegen.swift.entity.registry

import com.stanfy.helium.handler.codegen.swift.entity.entities.*
import com.stanfy.helium.internal.utils.Names
import com.stanfy.helium.model.Dictionary
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType
import com.stanfy.helium.model.constraints.EnumConstraint

interface SwiftTypeRegistry {

  fun registerSwiftType(heliumType: Type): SwiftEntity
  fun registerEnumType(heliumType: Type): SwiftEntityEnum?
  fun registerMappings(mappings: Map<String, String>)
  fun simpleSequenceType(heliumType: Type): SwiftEntityArray
  fun propertyName(fieldName: String): String
  fun className(heliumTypeName: String): String

  companion object {
    val EmptyResponse: SwiftEntity = SwiftEntityStruct("EmptyResponse")
  }
}

class SwiftTypeRegistryImpl : SwiftTypeRegistry {

  val registry = mutableMapOf<String, SwiftEntity>()

  override fun registerEnumType(heliumType: Type): SwiftEntityEnum? {
    val enum = enumType(heliumType) ?: return null
    registry.put(heliumType.name, enum)
    return enum
  }

  override fun registerSwiftType(heliumType: Type): SwiftEntity {
    return registry.getOrElse(heliumType.name) {
      val type: SwiftEntity =
          tryRegisterSequenceType(heliumType)
              ?: tryDictionary(heliumType)
              ?: tryPrimitiveType(heliumType)
              ?: structType(heliumType)
      registry.put(heliumType.name, type)
      return type
    }
  }

  override fun registerMappings(mappings: Map<String, String>) {
    registry.putAll(mappings.mapValues { name -> SwiftEntityStruct(name.value) })
  }


  private fun tryRegisterSequenceType(heliumType: Type): SwiftEntityArray? {
    if (heliumType !is Sequence) return null
    val itemType = registerSwiftType(heliumType.itemsType)
    return SwiftEntityArray(heliumType.name, itemType)
  }

  override fun simpleSequenceType(heliumType: Type): SwiftEntityArray {
    return SwiftEntityArray("", registerSwiftType(heliumType))
  }

  fun structType(heliumType: Type): SwiftEntityStruct {
    return SwiftEntityStruct(className(heliumType.name))
  }

  private fun tryPrimitiveType(heliumType: Type): SwiftEntityPrimitive? {
    return when (heliumType.name) {
      "int" -> SwiftEntityPrimitive("Int")
      "integer" -> SwiftEntityPrimitive("Int")
      "int32" -> SwiftEntityPrimitive("Int")
      "int64" -> SwiftEntityPrimitive("Int")
      "long" -> SwiftEntityPrimitive("Int")
      "double" -> SwiftEntityPrimitive("Double")
      "float" -> SwiftEntityPrimitive("Double")
      "float32" -> SwiftEntityPrimitive("Double")
      "float64" -> SwiftEntityPrimitive("Double")
      "string" -> SwiftEntityPrimitive("String")
      "bool" -> SwiftEntityPrimitive("Bool")
      "boolean" -> SwiftEntityPrimitive("Bool")
      else -> {
        null
      }
    }
  }

  private fun tryDictionary(heliumType: Type): SwiftEntityDictionary? {
    if (heliumType is Dictionary) {
      return SwiftEntityDictionary(registerSwiftType(heliumType.key), registerSwiftType(heliumType.value))
    }
    return null
  }

  fun enumType(heliumType: Type): SwiftEntityEnum? {
    if (heliumType !is ConstrainedType) return null
    val constraint = heliumType.constraints.first { con -> con is EnumConstraint } as? EnumConstraint<Any> ?: return null
    val enumValues = constraint.values
        .filterIsInstance<String>()
        .map { s ->
          SwiftEntityEnumCase(
              name = propertyName(s).capitalize(),
              value = s)
        }
    return SwiftEntityEnum(propertyName(heliumType.name).capitalize(), enumValues)

  }

  override fun className(heliumTypeName: String) : String {
    val prettifiedName = Names.prettifiedName(Names.canonicalName(heliumTypeName))
    if (arrayOf("Error").contains(prettifiedName)) {
      return "API" + prettifiedName
    }
    return prettifiedName
  }

  override fun propertyName(fieldName: String): String {
    val prettifiedName = Names.prettifiedName(Names.canonicalName(fieldName))
    if (arrayOf("enum", "default", "let", "case", "self", "description", "where").contains(prettifiedName)) {
      return prettifiedName + "Value"
    }
    return prettifiedName
  }

}