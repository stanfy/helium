package com.stanfy.helium.handler.codegen.swift.entity.entities

import com.stanfy.helium.handler.codegen.swift.entity.SwiftGenerationOptions
import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistry
import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistryImpl
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.constraints.ConstrainedType

interface SwiftEntitiesGenerator {
  fun entitiesFromHeliumProject(project: Project, options: SwiftGenerationOptions): List<SwiftEntity>
  fun entitiesFromHeliumProject(project: Project, options: SwiftGenerationOptions, typesRegistry: SwiftTypeRegistry): List<SwiftEntity>
}

class SwiftEntitiesGeneratorImpl : SwiftEntitiesGenerator {

  override fun entitiesFromHeliumProject(project: Project, options: SwiftGenerationOptions): List<SwiftEntity> {
    return entitiesFromHeliumProject(project, options, SwiftTypeRegistryImpl())
  }

  override fun entitiesFromHeliumProject(project: Project, options: SwiftGenerationOptions, typesRegistry: SwiftTypeRegistry): List<SwiftEntity> {
    typesRegistry.registerMappings(options.customTypesMappings)
    var filteredTypes = options.skipTypes
    val enums = project.types.all()
        .filterNot { type -> filteredTypes.contains(typesRegistry.className(type.name)) }
        .filterIsInstance<ConstrainedType>()
        .map { type ->
          typesRegistry.registerEnumType(type)
        }
        .filterNotNull()

    val messages = project.messages
        .filterNot { type -> filteredTypes.contains(typesRegistry.className(type.name)) }
        .filterNot { message -> message.isAnonymous }
        .map { message ->
          val props =
              message.parentPropertiesList().flatMap { it.fields }
              .filterNot { field -> field.isSkip }
              .map { field ->
                val type = if (field.isSequence) typesRegistry.simpleSequenceType(field.type) else typesRegistry.registerSwiftType(field.type)
                val hasDefaultValue = options.typeDefaultValues.contains(field.type.name)
                val fieldType = if (field.isRequired || hasDefaultValue) type else type.toOptional()
                SwiftProperty(typesRegistry.propertyName(field.name), fieldType, field.name)
              }
          SwiftEntityStruct(typesRegistry.registerSwiftType(message).name, props)
        }

    val sequences = project.sequences
        .filterNot { type -> filteredTypes.contains(typesRegistry.className(type.name)) }
        .filterNot { sequence -> sequence.isAnonymous }
        .map { sequence ->
          typesRegistry.registerSwiftType(sequence)
        }


    return enums + messages + sequences
  }

}
