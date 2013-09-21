package com.stanfy.helium.dsl

import com.stanfy.helium.model.MethodType
import spock.lang.Specification

/**
 * Spec for DSL entry point.
 */
class DslSpec extends Specification {

  /** Dsl instance under the test. */
  Dsl dsl = new Dsl()

  def "can describe services"() {
    when:
    dsl.service {
      name "Test service"
      description "service description"
      version 1.1
      location "http://api-example.com/api/$version"
    }
    dsl.service {
      name "Test service 2"
      description "service description 2"
      version "dev"
    }

    then:
    dsl.services.size() == 2
    dsl.services[0].name == "Test service"
    dsl.services[0].description == "service description"
    dsl.services[0].version == "1.1"
    dsl.services[0].location == "http://api-example.com/api/1.1"
    dsl.services[1].version == "dev"
  }

  def "can describe messages"() {
    when:
    dsl.message "A" spec {
      data(description : 'Data bytes', type : "bytes")
    }
    dsl.message "B" spec {
      id {
        required false
      }
      name {
        required true
      }
    }
    dsl.message "C" spec {
      id long optional
      name String required
    }

    then:
    dsl.messages.size() == 3
    dsl.messages[0].name == "A"
    dsl.messages[1].name == "B"
    dsl.messages[2].name == "C"

    dsl.messages[0].fields.size() == 1
    dsl.messages[0].fields[0].name == "data"
    dsl.messages[0].fields[0].required
    dsl.messages[0].fields[0].description == 'Data bytes'
    dsl.messages[0].fields[0].type.name == 'bytes'

    dsl.messages[1].fields.size() == 2
    dsl.messages[1].fields[0].name == "id"
    !dsl.messages[1].fields[0].required
    dsl.messages[1].fields[1].name == "name"
    dsl.messages[1].fields[1].required

    dsl.messages[2].fields.size() == 2
    dsl.messages[2].fields[0].name == "id"
    !dsl.messages[2].fields[0].required
    dsl.messages[2].fields[0].type.name == "int64"
    dsl.messages[2].fields[1].name == "name"
    dsl.messages[2].fields[1].required
    dsl.messages[2].fields[1].type.name == "string"
  }

  def "can describe service methods"() {
    when:
    dsl.message "PersonProfile" spec { }
    dsl.service {
      get "/person/${id}" spec {
        name 'Get Person'
        description 'bla bla bla'
        parameters {
          full 'bool' required
          friends(type : 'bool', description : 'whether to include the friends list to the response')
        }
        response PersonProfile
      }
    }

    then:
    dsl.services[0].methods[0].type == MethodType.GET
    dsl.services[0].methods[0].type == MethodType.GET

  }

}
