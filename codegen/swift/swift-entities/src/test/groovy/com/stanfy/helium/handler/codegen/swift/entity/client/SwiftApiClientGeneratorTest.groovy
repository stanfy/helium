package com.stanfy.helium.handler.codegen.swift.entity.client

import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistryImpl
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.handler.codegen.swift.entity.SwiftGenerationOptions
import com.stanfy.helium.model.Type
import spock.lang.Specification


class SwiftAPIClientGeneratorImplTest extends Specification {
  private SwiftAPIClientGeneratorImpl sut
  private ProjectDsl project
  private SwiftGenerationOptions options
  private SwiftTypeRegistryImpl typesRegistry

  def setup() {
    sut = new SwiftAPIClientGeneratorImpl()
  }

  def "should generate three files with correct names"() {
    given:
    project = new ProjectDsl()
    typesRegistry = new SwiftTypeRegistryImpl()
    options = new SwiftGenerationOptions()
    options.apiManagerName = "AnyAPIManager"

    when:
    def list = sut.clientFilesFromHeliumProject(project, typesRegistry, options)

    then:
    list.size() == 3
    list[0].name == "SwiftAPIClientCore"
    list[1].name == "SwiftAPIServiceExample"
    list[2].name == "SwiftAnyAPIManager"
  }
}

class SwiftAPIClientSimpleGeneratorImplTest extends Specification {
  private SwiftAPIClientSimpleGeneratorImpl sut
  private ProjectDsl project
  private SwiftGenerationOptions options
  private SwiftTypeRegistryImpl typesRegistry

  def setup() {
    sut = new SwiftAPIClientSimpleGeneratorImpl()
  }

  def "should generate single file with api manager only"() {
    given:
    project = new ProjectDsl()
    typesRegistry = new SwiftTypeRegistryImpl()
    options = new SwiftGenerationOptions()
    options.apiManagerName = "AnyAPIManager"

    when:
    def list = sut.clientFilesFromHeliumProject(project, typesRegistry, options)

    then:
    list.size() == 1
    list[0].name == "SwiftAnyAPIManager"
  }
}

class SwiftServicesMapHelperTest extends Specification {
  private SwiftServicesMapHelper sut
  private SwiftGenerationOptions generationOptions
  private ProjectDsl project
  private SwiftTypeRegistryImpl typesRegistry
  private Type strType


  def setup() {
    sut = new SwiftServicesMapHelper()
    project = new ProjectDsl()
    generationOptions = new SwiftGenerationOptions()
    typesRegistry = new SwiftTypeRegistryImpl()
    strType = new Type(name: "string")
    typesRegistry.registerEnumType(strType)
    typesRegistry.registerEnumType(new Type(name: "int32"))
  }

  def "path parameters"() {
    given:
    def method = new ServiceMethod()
    method.path = "/path/@param1/{param2}/{param3}"

    expect:
    sut.formattedPathForServiceMethod(method) == '/path/\\(param1)/\\(param2)/\\(param3)'
  }

  def "path extensions for parameters"() {
    given:
    def method = new ServiceMethod()

    Message testType = new Message(name: 'TestType')
    testType.addField(new Field(name: 'serial', type: strType))
    testType.addField(new Field(name: 'device_type', type: strType))
    testType.addField(new Field(name: 'show_hidden', type: new Type(name: "int32")))

    method.parameters = testType

    generationOptions.passURLparams = true

    and:
    def list = sut.formattedPathExtensionsForServiceMethod(method, typesRegistry, generationOptions)

    expect:
    list.size() == 3
    list.first().name == "serial"
    list.first().value == "serial"
    list.first().separator == "?"
    list[1].name == "device_type"
    list[1].value == "deviceType"
    list[1].separator == "&"
    list.last().name == "show_hidden"
    list.last().value == "String(describing: showHidden)"
    list.last().separator == "&"
  }

  def "should generate correct output for query and body"() {
    given:
    generationOptions.passURLparams = true
    project.type 'int32' spec { }
    project.type 'float' spec { }
    project.type 'string' spec { }
    project.type 'AMessage' message { name 'string' }
    project.type 'BMessage' message { extended_name 'string' }
    project.type 'BList' sequence 'BMessage'
    project.type 'DataContainer' message {
      data 'AMessage'
    }
    project.service {
      name "A"
      location "http://www.somewhere.com"

      post "/something/complex/@id" spec {
        name "Post something complex with id"
        parameters {
          include 'int32' required
        }
        body 'DataContainer'
        response 'BList'
      }

      get '/optional/parameters' spec {
        name 'optionalParametersTest'
        parameters {
          one 'int32' optional
          two 'string' optional
          three 'AMessage' optional
          four 'float' optional
          five 'int32' sequence
        }
      }
    }

    and:
    def list = sut.mapServices(project, typesRegistry, generationOptions)

    expect:
    list.size() == 1
    list[0].funcs.size() == 2

    list[0].funcs[0].name == "postSomethingComplexId"
    list[0].funcs[0].path.contains("id")
    list[0].funcs[0].parameterAsDictionary == ""
    list[0].funcs[0].interfaceParams.size() == 3

    list[0].funcs[0].interfaceParams[0].canonicalName == "id"
    list[0].funcs[0].interfaceParams[0].name == "id"
    list[0].funcs[0].interfaceParams[0].type == "String"
    list[0].funcs[0].interfaceParams[0].postfix == ".toJSONRepresentation()"

    list[0].funcs[0].interfaceParams[1].canonicalName == "include"
    list[0].funcs[0].interfaceParams[1].name == "include"
    list[0].funcs[0].interfaceParams[1].type == "Int"
    list[0].funcs[0].interfaceParams[1].postfix == ""

    list[0].funcs[0].interfaceParams[2].canonicalName == "data"
    list[0].funcs[0].interfaceParams[2].name == "data"
    list[0].funcs[0].interfaceParams[2].type == "AMessage"
    list[0].funcs[0].interfaceParams[2].postfix == ".toJSONRepresentation()"

    list[0].funcs[0].bodyParams.size() == 1
    list[0].funcs[0].pathExtensions.size() == 1

    list[0].funcs[1].name == "getOptionalParameters"
    list[0].funcs[1].interfaceParams.size() == 5
  }

  def "should generate correct output for body as dictionary"() {
    given:
    generationOptions.parametersPassingByDictionary = true
    project.type 'int32' spec { }
    project.type 'float' spec { }
    project.type 'string' spec { }
    project.type 'AMessage' message { name 'string' }
    project.type 'BMessage' message { extended_name 'string' }
    project.type 'SchemaLessMessage' message {  }
    project.type 'BList' sequence 'BMessage'
    project.type 'DataContainer' message {
      data 'AMessage'
    }
    project.service {
      name "A"
      location "http://www.somewhere.com"

      post "/some/complex/request" spec {
        name "Post something complex via dictionary"
        body 'DataContainer'
        response 'BList'
      }

      patch "/some/complex/request" spec {
        name "Post something complex via dictionary"
        body 'SchemaLessMessage'
        response 'BList'
      }
    }

    and:
    def list = sut.mapServices(project, typesRegistry, generationOptions)

    expect:
    list.size() == 1
    list[0].funcs.size() == 2

    list[0].funcs[0].name == "postSomeComplexRequest"
    list[0].funcs[0].parameterAsDictionary == "dataContainer.toJSONRepresentation() as! [String:Any]"
    list[0].funcs[0].interfaceParams.size() == 1
    list[0].funcs[0].bodyParams == null

    list[0].funcs[0].interfaceParams[0].canonicalName == "data"
    list[0].funcs[0].interfaceParams[0].name == "dataContainer"
    list[0].funcs[0].interfaceParams[0].type == "DataContainer"
    list[0].funcs[0].interfaceParams[0].postfix == ".toJSONRepresentation()"

    list[0].funcs[1].name == "patchSomeComplexRequest"
    list[0].funcs[1].interfaceParams[0].canonicalName == "data"
    list[0].funcs[1].interfaceParams[0].name == "schemaLessMessage"
    list[0].funcs[1].interfaceParams[0].type == "SchemaLessMessage"
    list[0].funcs[1].interfaceParams[0].postfix == ".toJSONRepresentation()"
  }

}
