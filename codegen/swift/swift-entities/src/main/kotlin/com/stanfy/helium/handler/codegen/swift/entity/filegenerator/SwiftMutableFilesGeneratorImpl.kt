package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.SwiftGenerationOptions
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityEnum
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntityStruct
import com.stanfy.helium.handler.codegen.swift.entity.mustache.SwiftTemplatesHelper

class SwiftMutableFilesGeneratorImpl : SwiftFilesGenerator {

  override fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile> {
    return this.filesFromEntities(entities, null)
  }

  override fun filesFromEntities(entities: List<SwiftEntity>, options: SwiftGenerationOptions?): List<SwiftFile> {
    val file: SwiftFile = object : SwiftFile {
      override fun name(): String {
        var nameValue = "EntitiesMutableExtensions"
        if (!options?.customFilePrefix.isNullOrEmpty())
          nameValue = options?.customFilePrefix + nameValue
        return nameValue
      }

      override fun contents(): String {
        val structs = entities
            .filterIsInstance<SwiftEntityStruct>()
            .map { entity ->
              SwiftTemplatesHelper.generateSwiftStructMutable(entity.name, entity.properties)
            }.joinToString(separator = "\n")

        return structs
      }
    }
    return listOf(file)
  }
}