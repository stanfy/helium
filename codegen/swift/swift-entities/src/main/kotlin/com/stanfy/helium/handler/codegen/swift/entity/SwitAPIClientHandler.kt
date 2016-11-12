package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.handler.codegen.swift.entity.client.SwiftAPIClientGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFile
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftOutputGenerator
import com.stanfy.helium.model.Project
import java.io.File

class SwiftAPIClientHandler(outputDirectory: File?, options: SwiftGenerationOptions?,
                            val apiClientGenerator: SwiftAPIClientGenerator,
                            val outputGenerator: SwiftOutputGenerator) :
    BaseGenerator<SwiftGenerationOptions>(outputDirectory, options), Handler {

  override fun handle(project: Project?) {
    val files: List<SwiftFile> = apiClientGenerator.clientFilesFromHeliumProject(project!!)
    outputGenerator.generate(outputDirectory, files)
  }
}

