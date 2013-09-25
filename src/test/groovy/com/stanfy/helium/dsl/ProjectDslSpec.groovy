package com.stanfy.helium.dsl

import com.stanfy.helium.model.MethodType
import com.stanfy.helium.DefaultTypesLoader
import spock.lang.Specification

/**
 * Spec for DSL entry point.
 */
class ProjectDslSpec extends Specification {

  /** Project instance under the test. */
  ProjectDsl dsl = new ProjectDsl()

  def "can describe simple types"() {
    when:
    dsl.type 'type1'
    dsl.type "${1 + 2}"
    dsl.type "with description" spec {
      description "very simple type"
    }
    def registeredNames = dsl.types.all().inject("") { String res, def type -> res + "$type.name;" }

    then:
    registeredNames.contains "type1;"
    registeredNames.contains "3;"
    !dsl.types.byName("with description").description.empty
  }

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
    DefaultTypesLoader.loadFor dsl
    int defaultTypesCount = dsl.types.all().collect { it }.size()
    dsl.type "A" message {
      data(description : 'Data bytes', type : "bytes")
    }
    dsl.type "B" message {
      id {
        required false
      }
      name {
        required true
        sequence true
      }
    }
    dsl.type "C" message {
      id long optional
      name String required
      list int sequence
    }

    then:
    dsl.messages.size() == 3
    dsl.messages.size() == dsl.types.all().collect { it }.size() - defaultTypesCount
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
    dsl.messages[1].fields[1].sequence

    dsl.messages[2].fields.size() == 3
    dsl.messages[2].fields[0].name == "id"
    !dsl.messages[2].fields[0].required
    dsl.messages[2].fields[0].type.name == "int64"
    dsl.messages[2].fields[1].name == "name"
    dsl.messages[2].fields[1].required
    dsl.messages[2].fields[1].type.name == "string"
    dsl.messages[2].fields[2].name == "list"
    dsl.messages[2].fields[2].type.name == "int32"
    dsl.messages[2].fields[2].sequence
    !dsl.messages[2].fields[2].required
  }

  def "can describe sequences"() {
    when:
    dsl.type "bool"
    dsl.type 'A' sequence 'bool'

    then:
    dsl.sequences[0].name == 'A'
  }

  def "can describe service methods"() {
    when:
    dsl.type "bool"
    dsl.type "PersonProfile" message { }
    dsl.service {
      get "/person/@id" spec {
        name 'Get Person'
        description 'bla bla bla'
        parameters {
          full 'bool' required
          friends(type : 'bool', description : 'whether to include the friends list to the response')
        }
        response "PersonProfile"
      }

      post "/person/@id" spec {
        name "Edit Person Profile"
        body "PersonProfile"
      }
    }

    then:
    dsl.services[0].methods[0].type == MethodType.GET
    dsl.services[0].methods[0].path == "/person/@id"
    dsl.services[0].methods[0].name == "Get Person"
    dsl.services[0].methods[0].description == "bla bla bla"
    dsl.services[0].methods[0].parameters.fields[0].name == "full"
    dsl.services[0].methods[0].parameters.fields[1].name == "friends"
    dsl.services[0].methods[0].response.name == "PersonProfile"
    dsl.services[0].methods[1].type == MethodType.POST
    dsl.services[0].methods[1].name == "Edit Person Profile"
    dsl.services[0].methods[1].body == dsl.services[0].methods[0].response

  }

  def "can describe notes"() {
    when:
    dsl.note "aha"
    dsl.note """and more"""
    dsl.note 'trivial'

    then:
    dsl.notes[0].value == 'aha'
    dsl.notes[1].value == 'and more'
    dsl.notes[2].value == 'trivial'
  }

  def "maintains structure sequence"() {
    when:
    dsl.note "note1"
    dsl.service {
      name "service1"
    }
    dsl.type "type1"
    dsl.note "note2"

    then:
    dsl.structure[0].value == "note1"
    dsl.structure[1].name == "service1"
    dsl.structure[2].name == "type1"
    dsl.structure[3].value == "note2"
  }

}
