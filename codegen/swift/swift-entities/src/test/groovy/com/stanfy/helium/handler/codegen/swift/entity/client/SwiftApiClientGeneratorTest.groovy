package com.stanfy.helium.handler.codegen.swift.entity.client

import com.stanfy.helium.model.ServiceMethod
import spock.lang.Specification

class SwiftApiClientGeneratorTest extends Specification {

  private SwiftServicesMapHelper servicesMapHelper

  def setup() {
    servicesMapHelper = new SwiftServicesMapHelper()
  }

  def "path parameters"() {
    given:
    def sm = new ServiceMethod()
    sm.path = "/path/@param1/{param2}/{param3}"

    expect:
    servicesMapHelper.formattedPathForServiceMethod(sm) == '/path/\\(param1)/\\(param2)/\\(param3)'
  }

}
