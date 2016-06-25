package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFilesGenerator
import com.stanfy.helium.model.Project
import java.io.File

/**
 * Created by paultaykalo on 6/25/16.
 */
class SwiftEntitiesGenerator(outputDirectory: File?, options: SwiftGenerationOptions?) :
    BaseGenerator<SwiftGenerationOptions>(outputDirectory, options), Handler {

  override fun handle(project: Project?) {
    val filesGenerator = SwiftFilesGenerator()
    filesGenerator.generate(outputDirectory, emptyList())
  }

}