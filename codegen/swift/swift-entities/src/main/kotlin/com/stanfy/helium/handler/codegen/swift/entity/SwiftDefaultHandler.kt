package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntitiesGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFilesGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftOutputGenerator
import com.stanfy.helium.model.Project
import java.io.File

/***
 * Default project handler that performs chain of transformations on DSL Project
 * DSL Project -> Swift Entities -> Swift Files -> Output
 */
class SwiftDefaultHandler(outputDirectory: File?, options: SwiftGenerationOptions?,
                          val entitiesGenerator: SwiftEntitiesGenerator,
                          val filesGenerator: Array<SwiftFilesGenerator>,
                          val outputGenerator: SwiftOutputGenerator) :
    BaseGenerator<SwiftGenerationOptions>(outputDirectory, options), Handler {

  override fun handle(project: Project?) {
    val entities = entitiesGenerator.entitiesFromHeliumProject(project!!, options?.customTypesMappings, options?.typeDefaultValues)
    filesGenerator.forEach { generator ->
      val files = generator.filesFromEntities(entities, options)
      outputGenerator.generate(outputDirectory, files)
    }
  }

}