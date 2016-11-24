package com.stanfy.helium.handler.codegen.swift.entity.client

import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFile
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFileImpl
import com.stanfy.helium.handler.codegen.swift.entity.mustache.SwiftTemplatesHelper.Companion.generatedTemplateWithName
import com.stanfy.helium.internal.utils.Names
import com.stanfy.helium.model.Project

interface SwiftAPIClientGenerator {
  fun clientFilesFromHeliumProject(project: Project): List<SwiftFile>
}


class SwiftAPIClientGeneratorImpl : SwiftAPIClientGenerator {
  override fun clientFilesFromHeliumProject(project: Project): List<SwiftFile> {
    // TODO : Different clientFilesFromHeliumProject? as an option

    val responseFilename = "SwiftAPIClientResponse"

    val functions =
        project.services.flatMap { service ->
          service.methods.map { serviceMethod ->
            val functionParams = serviceMethod.parameters?.fields ?: listOf()
            object {
              val name = Names.decapitalize(Names.prettifiedName(serviceMethod.name))
              val responseName = responseFilename
              val params = functionParams.map { parameter ->
                object {
                  val name = parameter.name
                  val thetype = parameter.type.name
                  val delimiter = ", "
                  val comment = parameter.description
                }
              }
              val enum_name = Names.prettifiedName(serviceMethod.name)
              val param_start = if (functionParams.size == 0) "" else "("
              val param_end = if (functionParams.size == 0) "" else ")"
            }
          }
        }

    return listOf(
        SwiftFileImpl(
            name = "SwiftAPIClientCore",
            contents = generatedTemplateWithName("client/SwiftAPIClientCore.mustache", functions)
        ),
        SwiftFileImpl(
            name = "SwiftAPIServiceExample",
            contents = generatedTemplateWithName("client/SwiftAPIServiceExample.mustache", object: Any () {})
        )
    )
  }
}