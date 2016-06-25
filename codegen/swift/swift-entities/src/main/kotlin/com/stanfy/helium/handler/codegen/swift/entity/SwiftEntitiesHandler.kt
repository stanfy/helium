package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntitiesGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFilesGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftOutputGenerator
import com.stanfy.helium.model.Project
import java.io.File


class SwiftEntitiesHandler(outputDirectory: File?, options: SwiftGenerationOptions?,
                           val entitiesGenerator: SwiftEntitiesGenerator,
                           val filesGenerator: SwiftFilesGenerator,
                           val outputGenerator: SwiftOutputGenerator) :
    BaseGenerator<SwiftGenerationOptions>(outputDirectory, options), Handler {

  override fun handle(project: Project?) {
    val entites = entitiesGenerator.entitiesFromHeliumProject(project!!)
    val files = filesGenerator.filesFromEntities(entites)
    outputGenerator.generate(outputDirectory, files)
  }

}