package com.stanfy.helium.handler.codegen.swift.entity

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.codegen.BaseGenerator
import com.stanfy.helium.handler.codegen.swift.entity.client.SwiftAPIClientGenerator
import com.stanfy.helium.handler.codegen.swift.entity.entities.SwiftEntitiesGenerator
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFile
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftOutputGenerator
import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistry
import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistryImpl
import com.stanfy.helium.model.Project
import java.io.File

class SwiftAPIClientHandler(outputDirectory: File?, options: SwiftGenerationOptions?,
                            val apiClientGenerator: SwiftAPIClientGenerator,
                            val entitiesGenerator: SwiftEntitiesGenerator,
                            val outputGenerator: SwiftOutputGenerator) :
    BaseGenerator<SwiftGenerationOptions>(outputDirectory, options), Handler {

  override fun handle(proj: Project?) {

    val project = proj ?: return

    val typesRegistry = SwiftTypeRegistryImpl()
    entitiesGenerator.entitiesFromHeliumProject(project, options?.customTypesMappings, options?.typeDefaultValues, typesRegistry)
    val files: List<SwiftFile> = apiClientGenerator.clientFilesFromHeliumProject(project, typesRegistry, options.apiManagerName)
    outputGenerator.generate(outputDirectory, files)
  }
}

