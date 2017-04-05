package com.stanfy.helium.handler.codegen.swift.entity.client

import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFile
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFileImpl
import com.stanfy.helium.handler.codegen.swift.entity.mustache.SwiftTemplatesHelper.Companion.generatedTemplateWithName
import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistry
import com.stanfy.helium.internal.utils.Names
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.ServiceMethod

interface SwiftAPIClientGenerator {
  fun clientFilesFromHeliumProject(project: Project, typesRegistry: SwiftTypeRegistry): List<SwiftFile>
}


data class ParameterDescription(val name: String, val type: String, val comment: String = "", val delimiter: String = ", ")

class SwiftAPIClientGeneratorImpl : SwiftAPIClientGenerator {
  override fun clientFilesFromHeliumProject(project: Project, typesRegistry: SwiftTypeRegistry): List<SwiftFile> {
    val responseFilename = "SwiftAPIClientResponse"

    val services =
        project.services.map { service ->
          object {
            var location = service.location
            val funcs = service.methods.map { serviceMethod ->

              val path = formattedPathForServiceMethod(serviceMethod)

              val functionParams = serviceMethod.parameters?.fields ?: listOf()
              val bodyParams = (serviceMethod.body as? Message)?.activeFields ?: listOf()

              val functionAndBodyParams = (functionParams + bodyParams).map { field ->
                ParameterDescription(
                    name = typesRegistry.propertyName(field.name),
                    type = typesRegistry.registerSwiftType(field.type).name,
                    comment = field.description ?: ""
                )
              }

              val pathParams = serviceMethod.pathParameters.map { name ->
                ParameterDescription(
                    name = name,
                    type = "String"
                )
              }

              val bottomParams = functionAndBodyParams
                  .mapLast { it.copy( delimiter = "") }

              val topParams = (pathParams + functionAndBodyParams)
                  .mapLast { it.copy( delimiter = "") }

              object {
                val name = Names.decapitalize(Names.prettifiedName(serviceMethod.canonicalName))
                val route = Names.capitalize(Names.prettifiedName(serviceMethod.canonicalName))
                val responseName = responseFilename
                val interfaceParams = topParams
                val bodyParams = bottomParams
                val hasBodyParams = bodyParams.isNotEmpty()
                val method = serviceMethod.type.toString()
                val encoding = if (serviceMethod.type.hasBody) "JSON" else "URL"
                val path = path
                var return_type = (if (serviceMethod.response != null) {
                   typesRegistry.registerSwiftType(serviceMethod.response)
                 } else {
                   SwiftTypeRegistry.EmptyResponse
                 }).name
              }
            }
          }
        }
    return listOf(
        SwiftFileImpl(
            name = "SwiftAPIClientCore",
            contents = generatedTemplateWithName("client/SwiftAPIClientCore.mustache")
        ),
        SwiftFileImpl(
            name = "SwiftAPIServiceExample",
            contents = generatedTemplateWithName("client/SwiftAPIServiceExample.mustache")
        ),
        SwiftFileImpl(
            name = "SwiftAPIRequestManager",
            contents = generatedTemplateWithName("client/SwiftAPIRequestManager.mustache", object : Any() {
              val services = services
            })
        )
    )
  }

  fun formattedPathForServiceMethod(serviceMethod: ServiceMethod): String {
    var res = serviceMethod.path
    serviceMethod.pathParameters.forEach { name ->
      res = res.replace("@$name", "\\($name)")
      res = res.replace("\\{$name\\}", "\\($name)")
    }
    return res
  }

}
inline fun <T> List<T>.mapLast(transform: (T) -> T): List<T> {
  val lastElement = lastOrNull() ?: return this
  return this.dropLast(1) + transform(lastElement)
}
