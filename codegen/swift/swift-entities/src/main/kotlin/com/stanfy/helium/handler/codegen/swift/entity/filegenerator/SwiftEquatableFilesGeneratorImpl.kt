package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.SwiftGenerationOptions
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnum
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityStruct
import com.stanfy.helium.handler.codegen.swift.entity.mustache.SwiftTemplatesHelper

class SwiftEquatableFilesGeneratorImpl : SwiftFilesGenerator {

  override fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile> {
    return this.filesFromEntities(entities, null)
  }

  override fun filesFromEntities(entities: List<SwiftEntity>, options: SwiftGenerationOptions?): List<SwiftFile> {
    // TODO : Different files? as an option
    val file: SwiftFile = object : SwiftFile {
      override fun name(): String {
        return "EntitiesEquatableExtensions"
      }

      override fun contents(): String {
        val structs = entities
            .filterIsInstance<SwiftEntityStruct>()
            .map { entity ->
              SwiftTemplatesHelper.generateSwiftStructEquatable(entity.name, entity.properties)
            }.joinToString(separator = "\n")

        val enums = entities
            .filterIsInstance<SwiftEntityEnum>()
            .map { entity ->
              SwiftTemplatesHelper.generateSwiftEnumEquatable(entity.name, entity.values)
            }.joinToString(separator = "\n")


        return arrayOf(enums, structs)
            .joinToString(separator = "\n")
      }
    }
    return listOf(file)
  }
}