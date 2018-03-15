package com.stanfy.helium.handler.codegen.swift.entity.client

import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFile
import com.stanfy.helium.handler.codegen.swift.entity.filegenerator.SwiftFileImpl
import com.stanfy.helium.handler.codegen.swift.entity.mustache.SwiftTemplatesHelper.Companion.generatedTemplateWithName
import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistry
import com.stanfy.helium.handler.codegen.swift.entity.SwiftGenerationOptions
import com.stanfy.helium.internal.utils.Names
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.ServiceMethod

interface SwiftAPIClientGenerator {
    fun clientFilesFromHeliumProject(project: Project, typesRegistry: SwiftTypeRegistry, options: SwiftGenerationOptions): List<SwiftFile>
}

data class ParameterDescription(val canonicalName: String,
                                val name: String,
                                val type: String,
                                val comment: String = "",
                                val delimiter: String = ", ",
                                val postfix: String = ".toJSONRepresentation()")
data class PathExtension(val name: String,
                         val value: String,
                         val separator: Char = '&')

data class ServiceMappedObject(val location: String,
                               val funcs: List<Any>)

data class MethodMappedObject(val name: String,
                              val route: String,
                              val responseName: String,
                              val interfaceParams: List<ParameterDescription>,
                              val bodyParams: List<ParameterDescription>?,
                              val parameterAsDictionary: String,
                              val isParameterAsDictionary: Boolean,
                              val hasBodyParams: Boolean,
                              val method: String,
                              val encoding: String,
                              val path: String,
                              val pathExtensions: List<Any>,
                              val return_type: String)

class SwiftServicesMapHelper {

  fun mapServices(project: Project, typesRegistry: SwiftTypeRegistry, options: SwiftGenerationOptions) : List<Any> {
    val responseFilename = "SwiftAPIClientResponse"

    return project.services.map { service ->
      ServiceMappedObject(
        location = service.location,
        funcs = service.methods.map { serviceMethod ->

          val path = formattedPathForServiceMethod(serviceMethod)
          val functionParams = serviceMethod.parameters?.fields ?: listOf()
          // Path extensions are extending URI by passing correct arguments
          val pathExtensions = formattedPathExtensionsForServiceMethod(serviceMethod, typesRegistry, options)
          val bodyMessage = serviceMethod.body as? Message
          val bodyType = serviceMethod.body
          var wholeTypeParameter = ""

          // Function parameters are easy to parse
          var functionParamsMapped = functionParams.map { field ->
            ParameterDescription(
                    canonicalName = typesRegistry.propertyName(field.name),
                    name = typesRegistry.propertyName(field.name),
                    type = typesRegistry.registerSwiftType(field.type).name,
                    comment = field.description ?: "",
                    postfix = if (field.type.isPrimitive()) "" else ".toJSONRepresentation()"
            )
          }

          var bodyParamsMapped: List<ParameterDescription> = listOf()
          // Check the options and generate code
          if (options.parametersPassingByDictionary == false) {
            // Simple way is just put all names in actual parameters, nothing more special will be there
            if (bodyMessage != null) {
              // And in both cases there's possible variations how to put parameters
              var bodyMessageActiveFields = bodyMessage.activeFields ?: listOf()
              bodyParamsMapped = bodyMessageActiveFields
                      .map { field ->
                        ParameterDescription(
                                canonicalName = typesRegistry.propertyName(field.name),
                                name = typesRegistry.propertyName(field.name),
                                type = typesRegistry.registerSwiftType(field.type).name,
                                comment = field.description ?: "",
                                postfix = if (field.type.isPrimitive()) "" else ".toJSONRepresentation()"
                        )
                      }
              functionParamsMapped = functionParamsMapped + bodyParamsMapped
            }
          } else {
            // This kind of output-handling will put the single parameter as a strictly defined typed item
            // for the message (or type). Usually, it's needed to pass dictionaries with varieties of values with no schema
            // Body message could be any of either message or type
            if (bodyMessage != null) {
              val parameterType = typesRegistry.registerSwiftType(bodyMessage).name
              val postfix = if (bodyMessage.isPrimitive()) " as! [String:Any]"
              else ".toJSONRepresentation() as! [String:Any]"
              wholeTypeParameter = Names.decapitalize(bodyMessage.name) + postfix
              var bodyAsSingleTypeInstance = ParameterDescription(canonicalName = "data", name = Names.decapitalize(bodyMessage.name), type = parameterType)
              functionParamsMapped = functionParamsMapped + listOf(bodyAsSingleTypeInstance)
            } else if (bodyType != null) {
              val parameterType = typesRegistry.registerSwiftType(bodyType).name
              val postfix = " as! [String:Any]"
              wholeTypeParameter = Names.decapitalize(bodyType.name) + postfix
              var bodyAsSingleTypeInstance = ParameterDescription(canonicalName = "data", name = Names.decapitalize(bodyType.name), type = parameterType)
              functionParamsMapped = functionParamsMapped + listOf(bodyAsSingleTypeInstance)
            }
          }

          val pathParams = serviceMethod.pathParameters.map { name ->
            ParameterDescription(
                    canonicalName = name,
                    name = name,
                    type = "String"
            )
          }

          val bodyParams = if (wholeTypeParameter.isEmpty()) bodyParamsMapped
                  .map { it.copy(delimiter = ",") }
                  .mapLast { it.copy(delimiter = "") }
          else null

          val interfaceParams = (pathParams + functionParamsMapped)
                  .mapLast { it.copy(delimiter = "") }

          MethodMappedObject(
            name = Names.decapitalize(Names.prettifiedName(serviceMethod.canonicalName)),
            route = Names.decapitalize(Names.prettifiedName(serviceMethod.canonicalName)),
            responseName = responseFilename,
            interfaceParams = interfaceParams,
            bodyParams = bodyParams,
            parameterAsDictionary = wholeTypeParameter,
            isParameterAsDictionary = wholeTypeParameter.isNotEmpty(),
            hasBodyParams = wholeTypeParameter.isEmpty() && bodyMessage != null,
            method = serviceMethod.type.toString(),
            encoding = if (serviceMethod.type.hasBody) "JSON" else "URL",
            path = path,
            pathExtensions = pathExtensions,
            return_type = (if (serviceMethod.response != null) {
              typesRegistry.registerSwiftType(serviceMethod.response)
            } else {
              SwiftTypeRegistry.EmptyResponse
            }).name
          )
        }
      )
    }
  }

  fun formattedPathForServiceMethod(serviceMethod: ServiceMethod): String {
    var res = serviceMethod.path
    serviceMethod.pathParameters.forEach { name ->
      val replacement = "\\($name)"
      res = res.replace("@$name", replacement).replace("{$name}", replacement)
    }
    return res
  }

  fun formattedPathExtensionsForServiceMethod(serviceMethod: ServiceMethod, typesRegistry: SwiftTypeRegistry, options: SwiftGenerationOptions) : List<Any> {
    var res = listOf<Any>()
    if (options.passURLparams) {
      val functionParams = serviceMethod.parameters?.fields ?: listOf()
      if (functionParams.isNotEmpty()) {
        res = functionParams.map { field ->
                  var castValue = typesRegistry.propertyName(field.name)
                  if (field.type.name != "string") {
                    castValue = "String(describing: ${castValue})"
                  }
                  PathExtension(
                          name = field.name,
                          value = castValue
                  )
                }
                .mapFirst { it.copy(separator = '?') }
      }
    }
    return res
  }
}

class SwiftAPIClientSimpleGeneratorImpl : SwiftAPIClientGenerator {
  override fun clientFilesFromHeliumProject(project: Project, typesRegistry: SwiftTypeRegistry, options: SwiftGenerationOptions): List<SwiftFile> {
      val helper = SwiftServicesMapHelper()
      val services = helper.mapServices(project, typesRegistry, options)

      return listOf(
        SwiftFileImpl(
            name = "Swift" + options.apiManagerName,
            contents = generatedTemplateWithName("client/SwiftAPIRequestManager.mustache", object : Any() {
              val services = services
              val requestManagerAlias = options.apiManagerName
              val routeEnumName = options.routeEnumName
            })
        )
    )
  }
}

class SwiftAPIClientGeneratorImpl : SwiftAPIClientGenerator {
    override fun clientFilesFromHeliumProject(project: Project, typesRegistry: SwiftTypeRegistry, options: SwiftGenerationOptions): List<SwiftFile> {
        val helper = SwiftServicesMapHelper()
        val services = helper.mapServices(project, typesRegistry, options)

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
                name = "Swift" + options.apiManagerName,
                contents = generatedTemplateWithName("client/SwiftAPIRequestManager.mustache", object : Any() {
                  val services = services
                  var requestManagerAlias = options.apiManagerName
                  val routeEnumName = options.routeEnumName
                })
            )
        )
    }
}

inline fun <T> List<T>.mapLast(transform: (T) -> T): List<T> {
  val lastElement = lastOrNull() ?: return this
  return this.dropLast(1) + transform(lastElement)
}

inline fun <T> List<T>.mapFirst(transform: (T) -> T): List<T> {
  val firstElement = firstOrNull() ?: return this
  return listOf(transform(firstElement)) + this.drop(1)
}
