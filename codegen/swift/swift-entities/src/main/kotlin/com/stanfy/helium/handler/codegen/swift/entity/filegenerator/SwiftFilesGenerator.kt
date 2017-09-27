package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.SwiftEntitiesAccessLevel
import com.stanfy.helium.handler.codegen.swift.entity.SwiftEntitiesType
import com.stanfy.helium.handler.codegen.swift.entity.SwiftGenerationOptions
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityArray
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnum
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityStruct
import com.stanfy.helium.handler.codegen.swift.entity.mustache.SwiftTemplatesHelper

/**
 * Transform entities to the list of files
 * Make sure that this class doesn't do any logic on names/values transformation
 * All values and names for entities need to be set up before this class comes in
 */
interface SwiftFilesGenerator {
  fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile>
  fun filesFromEntities(entities: List<SwiftEntity>, options: SwiftGenerationOptions?): List<SwiftFile>
}

class SwiftEntityFilesGeneratorImpl : SwiftFilesGenerator {
  override fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile> {
    return this.filesFromEntities(entities, null)
  }

  override fun filesFromEntities(entities: List<SwiftEntity>, options: SwiftGenerationOptions?): List<SwiftFile> {
    val file: SwiftFile = object : SwiftFile {
      override fun name(): String {
        var nameValue = "Entities"
        if (!options?.customFilePrefix.isNullOrEmpty())
          nameValue = options?.customFilePrefix + nameValue
        return nameValue
      }

      override fun contents(): String {
        val accessLevel = options?.entitiesAccessLevel ?: SwiftEntitiesAccessLevel.PUBLIC
        val entitiesType = options?.entitiesType ?: SwiftEntitiesType.STRUCT

        val structs = entities(entities, entitiesType, accessLevel)

        val enums = entities
            .filterIsInstance<SwiftEntityEnum>()
            .map { entity ->
              SwiftTemplatesHelper.generateSwiftEnum(entity.name, entity.values, accessLevel)
            }.joinToString(separator = "\n")

        val namedSequences = entities
            .filterIsInstance<SwiftEntityArray>()
            .filter { entity -> entity.name.length > 0 }
            .map { entity ->
              SwiftTemplatesHelper.generateSwiftTypeAlias(entity.name, entity, accessLevel)
            }.joinToString(separator = "\n")


        return arrayOf(namedSequences, enums, structs)
            .joinToString(separator = "\n")
      }

      private fun entities(entities: List<SwiftEntity>, type: SwiftEntitiesType, accessLevel: SwiftEntitiesAccessLevel): String {
        when (type) {
          SwiftEntitiesType.STRUCT ->
            return entities
                .filterIsInstance<SwiftEntityStruct>()
                .map { entity ->
                  SwiftTemplatesHelper.generateSwiftStruct(entity.name, entity.properties, accessLevel)
                }.joinToString(separator = "\n")

          SwiftEntitiesType.CLASS ->
            return entities
                .filterIsInstance<SwiftEntityStruct>()
                .map { entity ->
                  SwiftTemplatesHelper.generateSwiftClass(entity.name, entity.properties, accessLevel)
                }.joinToString(separator = "\n")
        }
      }
    }
    return listOf(file)
  }
}

class SwiftDecodableMappingsFilesGeneratorImpl : SwiftFilesGenerator {

  override fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile> {
    return this.filesFromEntities(entities, null)
  }

  override fun filesFromEntities(entities: List<SwiftEntity>, options: SwiftGenerationOptions?): List<SwiftFile> {
    var nameValue = "Mappings"
    if (!options?.customFilePrefix.isNullOrEmpty())
      nameValue = options?.customFilePrefix + nameValue
    return listOf(
        SwiftFileImpl(
            name = nameValue,
            contents = {
              val imports = "import Decodable.Decodable\nimport protocol Decodable.Decodable\nimport enum Decodable.DecodingError\nimport struct Decodable.KeyPath\n\n"
              val structs = entities
                  .filterIsInstance<SwiftEntityStruct>()
                  .map { entity ->
                    SwiftTemplatesHelper.generateSwiftStructDecodables(entity.name, entity.properties, options?.typeDefaultValues)
                  }.joinToString(separator = "\n")

              val enums = entities
                  .filterIsInstance<SwiftEntityEnum>()
                  .map { entity ->
                    SwiftTemplatesHelper.generateSwiftEnumDecodables(entity.name, entity.values)
                  }.joinToString(separator = "\n")


              arrayOf(imports, enums, structs)
                  .joinToString(separator = "\n")
            }()
        ))
  }
}

class SwiftTransformableDecodableMappingsFilesGeneratorImpl : SwiftFilesGenerator {

  override fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile> {
    return this.filesFromEntities(entities, null)
  }

  override fun filesFromEntities(entities: List<SwiftEntity>, options: SwiftGenerationOptions?): List<SwiftFile> {
    var nameValue = "TransformableMappings"
    if (!options?.customFilePrefix.isNullOrEmpty())
      nameValue = options?.customFilePrefix + nameValue
    return listOf(
            SwiftFileImpl(
            name = nameValue,
            contents = SwiftTemplatesHelper.generatedTemplateWithName("decodable/SwiftAPIDeserializable.mustache", object : Any() {
              val entities = entities
            })
        ))
  }
}
