package com.stanfy.helium.handler.codegen.swift.entity.filegenerator

import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntity
import com.stanfy.helium.handler.codegen.swift.entity.mustache.SwiftTamplatesHelper

interface SwiftFilesGenerator {
  fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile>
}

class SwiftFilesGeneratorImpl : SwiftFilesGenerator {
  override fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile> {
    // TODO : Different files?
    val file: SwiftFile = object : SwiftFile {
      override fun name(): String {
        return "Entities"
      }

      override fun contents(): String {
        // TODO : Non struct ?
        return entities.map { entity ->
          SwiftTamplatesHelper.generateSwiftStruct(entity.name, entity.properties)
        }.joinToString(separator = "\n")
      }
    }
    return listOf(file)
  }
}