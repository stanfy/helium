package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistry
import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistryImpl
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.constraints.ConstrainedType

interface SwiftEntitiesGenerator {
  fun entitiesFromHeliumProject(project: Project): List<SwiftEntity>
  fun entitiesFromHeliumProject(project: Project, customTypesMappings: Map<String, String>?): List<SwiftEntity>
  fun entitiesFromHeliumProject(project: Project, customTypesMappings: Map<String, String>?, defaultValues: Map<String, String>?): List<SwiftEntity>
  fun entitiesFromHeliumProject(project: Project, customTypesMappings: Map<String, String>?, defaultValues: Map<String, String>?, typesRegistry: SwiftTypeRegistry): List<SwiftEntity>
}

class SwiftEntitiesGeneratorImpl : SwiftEntitiesGenerator {
  override fun entitiesFromHeliumProject(project: Project): List<SwiftEntity> {
    return entitiesFromHeliumProject(project, null, null)
  }

  override fun entitiesFromHeliumProject(project: Project, customTypesMappings: Map<String, String>?): List<SwiftEntity> {
    return entitiesFromHeliumProject(project, customTypesMappings, null)
  }

  override fun entitiesFromHeliumProject(project: Project, customTypesMappings: Map<String, String>?, defaultValues: Map<String, String>?): List<SwiftEntity> {
    return entitiesFromHeliumProject(project, customTypesMappings, null, SwiftTypeRegistryImpl())
  }

  override fun entitiesFromHeliumProject(project: Project, customTypesMappings: Map<String, String>?, defaultValues: Map<String, String>?, typesRegistry: SwiftTypeRegistry): List<SwiftEntity> {
    if (customTypesMappings != null) {
      typesRegistry.registerMappings(customTypesMappings)
    }

    val enums = project.types.all()
        .filterIsInstance<ConstrainedType>()
        .map { type ->
          typesRegistry.registerEnumType(type)
        }
        .filterNotNull()

    val messages = project.messages
        .filterNot { message -> message.isAnonymous }
        .map { message ->
          val props =
              message.parentsTree().flatMap { it.fields }
              .filterNot { field -> field.isSkip }
              .map { field ->
                val type = if (field.isSequence) typesRegistry.simpleSequenceType(field.type) else typesRegistry.registerSwiftType(field.type)
                val hasDefaultValue = defaultValues?.contains(field.type.name) ?: false
                val fieldType = if (field.isRequired || hasDefaultValue) type else type.toOptional()
                SwiftProperty(typesRegistry.propertyName(field.name), fieldType, field.name)
              }
          SwiftEntityStruct(typesRegistry.registerSwiftType(message).name, props)
        }

    val sequences = project.sequences
        .filterNot { sequence -> sequence.isAnonymous }
        .map { sequence ->
          typesRegistry.registerSwiftType(sequence)
        }


    return enums + messages + sequences
  }

}

private fun Message.parentsTree(): List<Message> {
  return (if (hasParent()) parent.parentsTree() else listOf()) + listOf(this)
}