package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.internal.utils.Names
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType

interface SwiftEntitiesGenerator {
  fun entitiesFromHeliumProject(project: Project): List<SwiftEntity>
  fun swiftType(heliumType: Type): SwiftEntity
}

class SwiftEntitiesGeneratorImpl : SwiftEntitiesGenerator {
  override fun entitiesFromHeliumProject(project: Project): List<SwiftEntity> {
    val messages = project.messages
        .filterNot { message -> message.isAnonymous }
        .map { message ->
          val props = message.fields
              .filterNot { field -> field.isSkip }
              .map { field ->
            SwiftProperty(propertyName(field.name), swiftType(field.type))
          }
          SwiftEntityStruct(message.name, props)
        }
    val sequences = project.sequences
        .filterNot { sequence -> sequence.isAnonymous }
        .map { sequence ->
          swiftType(sequence)
        }

    val enums = project.types.all()
        .filter { type -> type is ConstrainedType }
        .map { type -> type as ConstrainedType }
        .filter { ctype -> ctype.constraints.size > 0}
        .map { ctype ->
          swiftType(ctype)
        }

    return enums + messages + sequences
  }

  fun propertyName(fieldName:String) :String {
    return Names.prettifiedName(Names.canonicalName(fieldName))
  }

  override fun swiftType(heliumType: Type): SwiftEntity {

    if (heliumType is ConstrainedType &&
        heliumType.constraints.size != 0) {
        return SwiftEntityEnum(heliumType.name, emptyList())
    }

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
        SwiftEntityStruct(heliumType.name)
      }
    }
  }
}