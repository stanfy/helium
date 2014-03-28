package com.stanfy.helium.dsl

import com.stanfy.helium.entities.json.ClosureJsonConverter
import com.stanfy.helium.model.Message
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
      SomeField(description : 'Some field', type : "string")
    }
    dsl.type "B" message {
      id {
        type 'int64'
        required false
      }
      name {
        type 'string'
        required true
        sequence true
      }
      'Date' {
        type 'int64'
      }
    }
    dsl.type "C" message {
      id long optional
      name String required
      list int sequence
      "Data" boolean optional
    }

    then:
    dsl.messages.size() == 3
    dsl.messages.size() == dsl.types.all().collect { it }.size() - defaultTypesCount
    dsl.messages[0].name == "A"
    dsl.messages[1].name == "B"
    dsl.messages[2].name == "C"

    dsl.messages[0].fields.size() == 2
    dsl.messages[0].fields[0].name == "data"
    dsl.messages[0].fields[0].required
    dsl.messages[0].fields[0].description == 'Data bytes'
    dsl.messages[0].fields[0].type.name == 'bytes'
    dsl.messages[0].fields[1].name == 'SomeField'

    dsl.messages[1].fields.size() == 3
    dsl.messages[1].fields[0].name == "id"
    dsl.messages[1].fields[0].type.name == "int64"
    !dsl.messages[1].fields[0].required
    dsl.messages[1].fields[1].name == "name"
    dsl.messages[1].fields[1].type.name == "string"
    dsl.messages[1].fields[1].required
    dsl.messages[1].fields[1].sequence
    dsl.messages[1].fields[2].name == "Date"

    dsl.messages[2].fields.size() == 4
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
    dsl.messages[2].fields[3].name == "Data"
  }

  def "can describe ignorable fields in messages"() {
    when:
    DefaultTypesLoader.loadFor dsl
    dsl.type 'A' message {
      normal 'string'
      ignore(skip: true)
    }

    then:
    dsl.messages[0].fields.size() == 2
    dsl.messages[0].fields[0].name == "normal"
    !dsl.messages[0].fields[0].skip
    dsl.messages[0].fields[1].name == "ignore"
    dsl.messages[0].fields[1].skip
    dsl.messages[0].fields[1].type != null
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
        response "bool"
      }

      delete "/person/@id" spec {
        // nothing
      }
    }

    then:
    dsl.services[0].methods[0].type == MethodType.GET
    dsl.services[0].methods[0].path == "/person/@id"
    dsl.services[0].methods[0].name == "Get Person"
    dsl.services[0].methods[0].description == "bla bla bla"
    dsl.services[0].methods[0].parameters.fields[0].name == "full"
    dsl.services[0].methods[0].parameters.fields[1].name == "friends"
    dsl.services[0].methods[0].parameters.anonymous
    dsl.services[0].methods[0].response.name == "PersonProfile"
    dsl.services[0].methods[1].type == MethodType.POST
    dsl.services[0].methods[1].name == "Edit Person Profile"
    dsl.services[0].methods[1].body == dsl.services[0].methods[0].response
    !dsl.services[0].methods[1].body.anonymous
    dsl.services[0].methods[2].type == MethodType.DELETE
    dsl.services[0].methods[2].body == null
    dsl.services[0].methods[2].response == null
    dsl.services[0].methods[2].parameters == null

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

  def "can describe service tests info"() {
    when:
    dsl.service {
      tests {
        useExamples true
        generateBadInputTests true
        httpHeaders {
          'header 1' 'value 1'
          'header 2' 'value 2'
        }
      }
    }

    then:
    dsl.services[0].testInfo.useExamples
    dsl.services[0].testInfo.generateBadInputTests
    dsl.services[0].testInfo.httpHeaders['header 1'] == 'value 1'
    dsl.services[0].testInfo.httpHeaders['header 2'] == 'value 2'
  }

  def "can describe method test info"() {
    when:
    dsl.type "123"
    dsl.service {
      get "/a/@param1/@param2" spec {
        response "123"
        tests {
          useExamples true
          pathExample {
            param1 "1"
            param2 "2"
          }
          httpHeaders {
            'header 1' 'value 1'
            'header 2' 'value 2'
          }
        }
      }
    }

    then:
    dsl.services[0].testInfo != null
    !dsl.services[0].testInfo.useExamples
    dsl.services[0].testInfo.httpHeaders != null
    dsl.services[0].testInfo.httpHeaders.isEmpty()

    dsl.services[0].methods[0].testInfo.useExamples
    dsl.services[0].methods[0].testInfo.pathExample.size() == 2
    dsl.services[0].methods[0].testInfo.httpHeaders['header 1'] == 'value 1'
    dsl.services[0].methods[0].testInfo.httpHeaders['header 2'] == 'value 2'
  }

  def "can describe test scenarios"() {
    when:
    dsl.service {
      tests {
        scenario "log in and get stream", after: {}, before: {} spec {
          def loginResult = post "user/login/@type" with {
            path {
              type "facebook"
            }
            parameters {
              fake false
            }
            body {
              token "abc!!!234MLK"
              email "john.doe@gmail.com"
            }
          }
          assert loginResult != null : "Bad login result"
          assert loginResult.resultCode == 0 : "Operation was not successful"
          def streamResult = get "stream/get" with {
            parameters {
              userId loginResult.someId
            }
            httpHeaders {
              "SESSION-ID" loginResult.session
            }
          }
          assert streamResult != null : "Bad stream result"
        }

        scenario "test" spec { }
      }
    }

    then:
    !dsl.services[0].testInfo.scenarios?.empty
    dsl.services[0].testInfo.scenarios[0].name == "log in and get stream"
    dsl.services[0].testInfo.scenarios[0].action != null
    dsl.services[0].testInfo.scenarios[0].before != null
    dsl.services[0].testInfo.scenarios[0].after != null
    dsl.services[0].testInfo.scenarios[1].name == "test"
    dsl.services[0].testInfo.scenarios[1].action != null
    dsl.services[0].testInfo.scenarios[1].before == null
    dsl.services[0].testInfo.scenarios[1].after == null
  }

  def "can use closures in type descriptions"() {
    when:
    dsl.type 'string'

    def closureConstructor = { Closure<?> mainSpec = null ->
      return {
        field1 'string'
        field2 'string'
        if (mainSpec) {
          // TODO: simplify it for a user
          mainSpec.delegate = delegate
          mainSpec()
        }
      }
    }

    dsl.type 'Type1' message closureConstructor({
      field3 'string'
    })
    dsl.type 'Type2' message closureConstructor()

    then:
    dsl.types.byName('Type1') instanceof Message
    (dsl.types.byName('Type1') as Message).fields.size() == 3
    (dsl.types.byName('Type2') as Message).fields.size() == 2
    (dsl.types.byName('Type1') as Message).fields[2].name == 'field3'
    (dsl.types.byName('Type2') as Message).fields[0].name == 'field1'

  }

  def "can describe type converters"() {
    given:
    dsl.type "custom" spec {
      description "Custom type"
      from("json") { asString() }
      to("json") { asDate("yyyy-MM-dd HH:mm:ss Z") }
    }
    def customType = dsl.types.byName("custom")
    def converter = dsl.types.findConverters("json")?.getConverter(customType)

    expect:
    customType != null
    converter instanceof ClosureJsonConverter
    converter.reader != null
    converter.writer != null

  }

  def "should allow empty response type"() {
    when:
    dsl.service {
      get '/aaa' spec { }
    }

    then:
    dsl.services[0].methods[0].response == null
  }

  def "can set skipping all unknown fields in message"() {
    when:
    dsl.type "MyType" message(skipUnknownFields: true) { }

    then:
    dsl.messages[0].skipUnknownFields
  }

}
