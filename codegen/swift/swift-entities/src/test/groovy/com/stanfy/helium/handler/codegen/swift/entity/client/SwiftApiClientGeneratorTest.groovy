package com.stanfy.helium.handler.codegen.swift.entity.client

import com.stanfy.helium.handler.codegen.swift.entity.registry.SwiftTypeRegistryImpl
import com.stanfy.helium.model.Field
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.handler.codegen.swift.entity.SwiftGenerationOptions
import com.stanfy.helium.model.Type

import spock.lang.Specification

class SwiftApiClientGeneratorTest extends Specification {

  private SwiftServicesMapHelper servicesMapHelper
  private SwiftGenerationOptions generationOptions

  def setup() {
    servicesMapHelper = new SwiftServicesMapHelper()
    generationOptions = new SwiftGenerationOptions()
  }

  def "path parameters"() {
    given:
    def method = new ServiceMethod()
    method.path = "/path/@param1/{param2}/{param3}"

    expect:
    servicesMapHelper.formattedPathForServiceMethod(method) == '/path/\\(param1)/\\(param2)/\\(param3)'
  }

  def "path extensions for parameters"() {
    given:
    def method = new ServiceMethod()

    Type str = new Type(name: 'string')
    Message testType = new Message(name: 'TestType')
    testType.addField(new Field(name : 'serial', type: str))
    testType.addField(new Field(name : 'device_type', type: str))
    testType.addField(new Field(name : 'show_hidden', type: new Type(name: "int32")))

    SwiftTypeRegistryImpl typesRegistry = new SwiftTypeRegistryImpl()
    typesRegistry.registerEnumType(str)
    typesRegistry.registerEnumType(new Type(name: "int32"))
    method.parameters = testType

    generationOptions.passURLparams = true

    and:
    def list = servicesMapHelper.formattedPathExtensionsForServiceMethod(method, typesRegistry, generationOptions)

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
}
