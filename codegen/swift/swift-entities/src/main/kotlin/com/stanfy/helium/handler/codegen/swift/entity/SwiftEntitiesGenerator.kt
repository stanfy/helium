package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFile
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFilesGenerator
import com.stanfy.helium.handler.codegen.swift.entity.mustache.SwiftTamplatesHelper
import com.stanfy.helium.model.Project
import java.io.File

/**
 * Created by paultaykalo on 6/25/16.
 */
class SwiftEntitiesGenerator(outputDirectory: File?, options: SwiftGenerationOptions?) :
    BaseGenerator<SwiftGenerationOptions>(outputDirectory, options), Handler {

  override fun handle(project: Project?) {
    val entites = entities(project!!)
    val files = filesFromEntities(entites)
    val filesGenerator = SwiftFilesGenerator()
    filesGenerator.generate(outputDirectory, files)
  }

  fun entities(project: Project): List<SwiftEntity> {
    return project.messages.map { message ->
      SwiftEntity(message.name)
    }
  }

  fun filesFromEntities(entities: List<SwiftEntity>): List<SwiftFile> {
    // TODO : Different files?
    val file: SwiftFile = object : SwiftFile {
      override fun name(): String {
        return "Entities"
      }

      override fun contents(): String {
        // TODO : Non struct
        return entities.map { entity ->
          SwiftTamplatesHelper.generateSwiftStruct(entity.name)
        }.joinToString(separator = "\n")
      }
    }
    return listOf(file)
  }


}