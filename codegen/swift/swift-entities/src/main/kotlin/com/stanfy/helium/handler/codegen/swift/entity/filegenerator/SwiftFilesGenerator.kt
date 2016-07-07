package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.SwiftEntitiesAccessLevel
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
    // TODO : Different files? as an option
    val file: SwiftFile = object : SwiftFile {
      override fun name(): String {
        return "Entities"
      }

      override fun contents(): String {
        val accessLevel = options?.entitiesAccessLevel ?: SwiftEntitiesAccessLevel.INTERNAL
        val structs = entities
            .filterIsInstance<SwiftEntityStruct>()
            .map { entity ->
              SwiftTemplatesHelper.generateSwiftStruct(entity.name, entity.properties, accessLevel)
            }.joinToString(separator = "\n")

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
    }
    return listOf(file)
  }
}

class SwiftDecodableMappingsFilesGeneratorImpl : SwiftFilesGenerator {

  override fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile> {
    return this.filesFromEntities(entities, null)
  }

  override fun filesFromEntities(entities: List<SwiftEntity>, options: SwiftGenerationOptions?): List<SwiftFile> {
    // TODO : Different files? as an option
    val file: SwiftFile = object : SwiftFile {
      override fun name(): String {
        return "Mappings"
      }

      override fun contents(): String {
        val imports = "import Decodable"
        val structs = entities
            .filterIsInstance<SwiftEntityStruct>()
            .map { entity ->
              SwiftTemplatesHelper.generateSwiftStructDecodables(entity.name, entity.properties)
            }.joinToString(separator = "\n")

        val enums = entities
            .filterIsInstance<SwiftEntityEnum>()
            .map { entity ->
              SwiftTemplatesHelper.generateSwiftEnumDecodables(entity.name, entity.values)
            }.joinToString(separator = "\n")


        return arrayOf(imports, enums, structs)
            .joinToString(separator = "\n")
      }
    }
    return listOf(file)
  }
}
