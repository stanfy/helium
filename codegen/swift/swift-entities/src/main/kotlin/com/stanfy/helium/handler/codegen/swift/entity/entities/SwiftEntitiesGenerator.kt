package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Type

interface SwiftEntitiesGenerator {
  fun entitiesFromHeliumProject(project: Project): List<SwiftEntity>;
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
            SwiftProperty(field.name, swiftType(field.type))
          }
          val swiftEntity = SwiftEntity(message.name, props)
          swiftEntity
        }
    val sequences = project.sequences
        .filterNot { sequence -> sequence.isAnonymous }
        .map { sequence ->
          swiftType(sequence)
        }

    return messages + sequences
  }

  override fun swiftType(heliumType: Type): SwiftEntity {
    return when (heliumType.name) {
      "int" -> SwiftEntity("Int")
      "integer" -> SwiftEntity("Int")
      "int32" -> SwiftEntity("Int")
      "int64" -> SwiftEntity("Int")
      "long" -> SwiftEntity("Int")
      "double" -> SwiftEntity("Double")
      "float" -> SwiftEntity("Double")
      "float32" -> SwiftEntity("Double")
      "float64" -> SwiftEntity("Double")
      "string" -> SwiftEntity("String")
      "bool" -> SwiftEntity("Bool")
      "boolean" -> SwiftEntity("Bool")
      else -> {
        SwiftEntity(heliumType.name)
      }
    }
  }
}