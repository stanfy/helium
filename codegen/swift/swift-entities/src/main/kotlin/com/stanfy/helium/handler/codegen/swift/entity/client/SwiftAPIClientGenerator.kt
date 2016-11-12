package com.stanfy.helium.handler.codegen.swift.entity.client

import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFile
import com.stanfy.helium.handler.codegen.swift.entity.mustache.SwiftTemplatesHelper
import com.stanfy.helium.internal.utils.Names
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.ServiceMethod

interface SwiftAPIClientGenerator {
  fun clientFilesFromHeliumProject(project: Project): List<SwiftFile>
}


class SwiftAPIClientGeneratorImpl : SwiftAPIClientGenerator {
  override fun clientFilesFromHeliumProject(project: Project): List<SwiftFile> {
    // TODO : Different clientFilesFromHeliumProject? as an option

    val functions =
    project.services.flatMap { service ->
      service.methods.map { function ->
        object {
          val name = Names.decapitalize(Names.prettifiedName(function.name))
        }
      }
    }

    val file: SwiftFile = object : SwiftFile {
      override fun name(): String {
        return "SwiftAPIClientRequestManager"
      }

      override fun contents(): String {
        return SwiftTemplatesHelper.generateSwiftAPIClientFunctions(functions)
      }
    }
    return listOf(file)
  }
}