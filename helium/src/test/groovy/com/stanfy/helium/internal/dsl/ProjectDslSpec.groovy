package com.stanfy.helium.internal.dsl

import com.squareup.okhttp.MediaType
import com.stanfy.helium.DefaultTypesLoader
import com.stanfy.helium.internal.model.tests.CheckableService
import com.stanfy.helium.model.DataType
import com.stanfy.helium.model.Dictionary
import com.stanfy.helium.model.FileType
import com.stanfy.helium.model.FormType
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.MultipartType
import com.stanfy.helium.model.Authentication
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.constraints.ConstrainedType
import com.stanfy.helium.model.tests.BehaviourCheck
import com.stanfy.helium.model.tests.BehaviourSuite
import com.stanfy.helium.model.tests.CheckListener
import com.stanfy.helium.model.tests.Oauth2AuthenticationParams
import org.joda.time.Duration
import spock.lang.Specification

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.*

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
      '@TypeWithAt' {
        type 'string'
        required true
        description 'Some description for field with at'
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

    dsl.messages[1].fields.size() == 4
    dsl.messages[1].fields[0].name == "id"
    dsl.messages[1].fields[0].type.name == "int64"
    !dsl.messages[1].fields[0].required
    dsl.messages[1].fields[1].name == "name"
    dsl.messages[1].fields[1].type.name == "string"
    dsl.messages[1].fields[1].required
    dsl.messages[1].fields[1].sequence
    dsl.messages[1].fields[2].name == "@TypeWithAt"
    dsl.messages[1].fields[2].type.name == "string"
    dsl.messages[1].fields[2].required
    dsl.messages[1].fields[2].description == 'Some description for field with at'
    dsl.messages[1].fields[3].name == "Date"

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
    dsl.messages[2].fields[2].required
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

  def "can describe dictionaries"() {
    when:
    dsl.type 'string'
    dsl.type 'int32'
    dsl.type 'A' dictionary(key: 'string', value: 'int32')
    dsl.type 'B' dictionary('string', 'int32')

    then:
    dsl.dictionaries[0].name == 'A'
    dsl.dictionaries[1].name == 'B'
    dsl.types.byName('A') instanceof Dictionary
    dsl.types.byName('A').key.name == 'string'
    dsl.types.byName('A').value.name == 'int32'
    dsl.types.byName('B').key.name == 'string'
    dsl.types.byName('B').value.name == 'int32'
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

  def "can set message parents" () {
    given:
    dsl.type "BaseMessage" message {}
    dsl.type "ChildMessage" message(parent: "BaseMessage") {}

    expect:
    def parent = dsl.types.byName("BaseMessage")
    def child = dsl.types.byName("ChildMessage")
    parent instanceof Message
    child instanceof Message
    !(parent as Message).hasParent()
    (child as Message).hasParent()
    (child as Message).getParent().name == parent.name
  }

  def "can detect unknown parents" () {
    when:
    dsl.type "MessageWithBadParent" message(parent: "Base") {}

    then:
    thrown(IllegalArgumentException)
  }

  def "should allow only messages as parents"() {
    when:
    dsl.type "123"
    dsl.type "MyInteger" message(parent: "123") {}

    then:
    def e = thrown IllegalArgumentException
    e.message.contains "Only messages"
  }

  def "should check if message is parent of itself"() {
    when:
    dsl.type "Samovar" message(parent: "Samovar") {}

    then:
    thrown IllegalArgumentException
  }

  def "can describe type converters"() {
    given:
    dsl.type "custom" spec {
      description "Custom type"
      from("json") { asString() }
      to("json") { asDate("yyyy-MM-dd HH:mm:ss Z") }
    }
    def customType = dsl.types.byName("custom")
    def reader = dsl.types.customReaders(MediaType.parse('*/json'))[customType]
    def writer = dsl.types.customWriters(MediaType.parse('*/json'))[customType]

    expect:
    customType != null
    reader != null
    writer != null
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

  def "can include other files"() {
    given:
    def file = new File(ProjectDslSpec.class.getResource("/included.spec").toURI())

    when:
    dsl.include file

    then:
    dsl.notes[-2].value == "I'm included"
    !dsl.notes[-1].value.empty
    dsl.includedFiles[-1] == file
  }

  def "ignores already included files"() {
    given:
    def file = new File(ProjectDslSpec.class.getResource("/included.spec").toURI())

    when:
    dsl.include file
    dsl.include file

    then:
    dsl.includedFiles.size() == 1
  }

  def "can do nested includes"() {
    given:
    def file = new File(ProjectDslSpec.class.getResource("/include-nested.spec").toURI())

    when:
    dsl.include file

    then:
    dsl.notes[-1].value == "I'm included 2"
    dsl.notes[-3].value == "I'm included"
    dsl.includedFiles[-2] == file
  }

  def "can user parseString for custom types"() {
    given:
    dsl.type "custom" spec {
      description "Custom type"
      from("json") { parseString { Integer.parseInt(it) } }
      to("json") { formatToString { String.valueOf(it) } }
    }
    def customType = dsl.types.byName("custom")
    def reader = dsl.types.customReaders(MediaType.parse('*/json'))[customType]
    def writer = dsl.types.customWriters(MediaType.parse('*/json'))[customType]

    expect:
    customType != null
    reader != null
    writer != null
  }

  def "can describe http headers for service methods"() {
    when:
    dsl.service {
      get "some" spec {
        httpHeaders 'header1', header('header2', 'value2', examples: ['example']),
            header('header3', value: 'value3'), header('header4'), header(name: 'header5'),
            "${'header6'}"
      }
    }
    def headers = dsl.services[0].methods[0].httpHeaders

    then:
    headers.size() == 6

    headers[0].name == 'header1'
    !headers[0].constant

    headers[1].name == 'header2'
    headers[1].constant
    headers[1].value == 'value2'
    headers[1].examples == ['example']

    headers[2].name == 'header3'
    headers[2].value == 'value3'
    headers[2].constant

    headers[3].name == 'header4'
    !headers[3].constant

    headers[4].name == 'header5'
    !headers[4].constant

    headers[5].name == 'header6'
    !headers[5].constant
  }

  def "can give int examples"() {
    when:
    dsl.type 'int32' spec { }
    dsl.type 'A' message {
      ttt(type: 'int32', examples: [1, 2, 3])
    }
    then:
    dsl.messages[0].fields[0].examples.size() == 3
  }

  def "declaration of duplicate methods is not allowed"() {
    when:
    dsl.service {
      name "t"
      get "/1" spec { }
      post "/1" spec { }
      get "/1" spec { }
    }
    then:
    def e = thrown(IllegalStateException)
    e.message.contains "GET /1"
  }

  def "can describe constrained types"() {
    when:
    dsl.type "string"
    dsl.type "branch" spec {
      constraints("string") {
        enumeration 'master', 'dev'
      }
    }
    then:
    dsl.types.byName("branch") instanceof ConstrainedType
    dsl.types.byName("branch").baseType.name == "string"
    dsl.types.byName("branch").constraints.size() == 1
  }

  def "can describe constrained fields"() {
    when:
    dsl.type "string"
    dsl.type "CMessage" message {
      branch(type: "string") {
        constraints {
          enumeration 'master', 'dev'
        }
      }
      versionPrefix(type: "string", required: false) {
        constraints {
          enumeration 'alpha', 'beta'
        }
      }
    }
    def msg = (Message) dsl.types.byName("CMessage")

    then:
    msg.fieldByName("branch").type instanceof ConstrainedType
    msg.fieldByName("branch").type.baseType.name == "string"
    msg.fieldByName("branch").type.constraints.size() == 1
    msg.fieldByName("branch").type.constraints[0].validate("master")
    !msg.fieldByName("branch").type.constraints[0].validate("feature1")
    msg.fieldByName("versionPrefix").type.constraints[0].validate("alpha")
    !msg.fieldByName("versionPrefix").type.constraints[0].validate("SNAPSHOT")
  }

  def "can describe head requests"() {
    when:
    dsl.type 'int32'
    dsl.service {
      name "head test"
      head "/" spec {
        response 'int32'
      }
    }

    then:
    dsl.serviceByName("head test").methods[0].type == MethodType.HEAD
  }

  def "can add service behaviour descriptions"() {
    when:
    dsl.service {
      name "test behaviour"
      describe "some behaviour" spec {
        throw new UnsupportedOperationException("This code is not executed yet")
      }
    }

    then:
    ((CheckableService) dsl.serviceByName("test behaviour")).checksCount() == 1
  }

  def "can add project behaviour descriptions"() {
    when:
    dsl.describe "some behaviour" spec {
      throw new UnsupportedOperationException("This code is not executed yet")
    }
    dsl.describe "some behaviour 2" spec {
      throw new UnsupportedOperationException("This code is not executed yet")
    }

    then:
    dsl.checksCount() == 2
  }

  def "service can be checked"() {
    given:
    def listener = Mock(CheckListener)

    when:
    dsl.service {
      name "test"
      describe "b1" spec {
        assert 1 == 1
      }
      describe "b2" spec {
        assert 1 == 0
      }
    }
    def results = dsl.serviceByName("test").check(null, listener)

    then:
    results.time >= Duration.ZERO
    results.result == FAILED
    results.children[0].result == PENDING
    results.children[1].result == FAILED
    3 * listener.onSuiteStarted(_ as BehaviourSuite)
    3 * listener.onSuiteDone(_ as BehaviourSuite)
  }

  def "project can be checked"() {
    given:
    def listener = Mock(CheckListener)

    when:
    int counter = 0
    dsl.describe "b1" spec {
      beforeEach { counter++ }
      it "should be cool", { 1 == 1 }
      it "should be great", { 2 == 2 }
      afterEach { counter++ }
    }
    dsl.describe "b2" spec {
      assert 1 == 0
    }
    def results = dsl.check(null, listener)

    then:
    results.time >= Duration.ZERO
    results.result == FAILED
    results.children[0].result == PASSED
    results.children[1].result == FAILED
    results.children[1].description.contains("1 == 0")
    counter == 4
    3 * listener.onSuiteStarted(_ as BehaviourSuite)
    3 * listener.onSuiteDone(_ as BehaviourSuite)
    2 * listener.onCheckStarted(_ as BehaviourCheck)
    2 * listener.onCheckDone(_ as BehaviourCheck)
  }

  def "pending project checks"() {
    given:
    def listener = Mock(CheckListener)

    when:
    int counter = 0
    dsl.describe "b1" spec {
      beforeEach { counter++ }
      it "should be cool and pass", { 1 == 1 }
      xit "should ignore this failure", { 2 == 3 }
      xit "has no implementation"
      it "is still ignored"

      describe "nested" spec {
        it "also pending"
      }

      afterEach { counter++ }
    }
    def results = dsl.check(null, listener)

    then:
    (results.children[0] as BehaviourSuite).children[0]?.result == PASSED
    (results.children[0] as BehaviourSuite).children[1]?.result == PENDING
    (results.children[0] as BehaviourSuite).children[2]?.result == PENDING
    (results.children[0] as BehaviourSuite).children[3]?.result == PENDING
    results.result == PENDING
    results.children[0].result == PENDING
    counter == 4
    5 * listener.onCheckStarted(_ as BehaviourCheck)
    5 * listener.onCheckDone(_ as BehaviourCheck)
    3 * listener.onSuiteStarted(_ as BehaviourSuite)
    3 * listener.onSuiteDone(_ as BehaviourSuite)
  }

  def "project behaviour can be nested in closure"() {
    given:
    def listener = Mock(CheckListener)
    when:
    dsl.describe "pb" spec {
      5.times { n ->
        describe "describe $n" spec {
          3.times { k ->
            it("should be $k") { }
          }
        }
      }
    }
    dsl.service {
      name "S"
      describe "sb" spec {
        5.times { n ->
          describe "describe s $n" spec {
            3.times { k ->
              it("should be s $k") { }
            }
          }
        }
      }
    }
    def pResults = dsl.check(null, listener)
    def sResults = dsl.serviceByName("S").check(null, listener)

    then:
    dsl.checksCount() == 1
    (dsl.serviceByName("S") as CheckableService).checksCount() == 1

    pResults.children.size() == 1
    pResults.children[0].name == "pb"
    def pb = pResults.children[0] as BehaviourSuite
    pb.children.size() == 5
    pb.children[3].name == "describe 3"
    (pb.children[2] as BehaviourSuite).children.size() == 3
    (pb.children[2] as BehaviourSuite).children[1].name == "should be 1"

    sResults.children.size() == 1
    sResults.children[0].name == "sb"
    def sb = sResults.children[0] as BehaviourSuite
    sb.children.size() == 5
    sb.children[3].name == "describe s 3"
    (sb.children[2] as BehaviourSuite).children.size() == 3
    (sb.children[2] as BehaviourSuite).children[1].name == "should be s 1"
  }

  def "certificate security definition"() {
    when:
    dsl.service {
      name 'Test service 1'
      authentication certificate() {
        description '''
          Explanations how to get a cert.
        '''
      }
    }
    dsl.service {
      name 'Another service 2'
      authentication basic(description: 'Test description')
      authentication oauth2()
    }

    then:
    dsl.services[0].name == 'Test service 1'
    !dsl.services[0].authentications.empty
    !dsl.services[1].authentications.empty
    dsl.services[0].authentications[0]?.type == Authentication.Type.CERTIFICATE
    dsl.services[0].authentications[0].description == 'Explanations how to get a cert.'
    dsl.services[0].authentications[0].name == 'certificate'
    dsl.services[1].name == 'Another service 2'
    dsl.services[1].authentications[0]?.type == Authentication.Type.BASIC
    dsl.services[1].authentications[0].description == 'Test description'
    dsl.services[1].authentications[0].name == 'basic'
    dsl.services[1].authentications[1].type == Authentication.Type.OAUTH2
    dsl.services[1].authentications[1].name == 'oauth2'
  }

  //region form request content type

  def "can handle form body by name"() {
    when:
    dsl.type 'int32'
    dsl.type 'FormType' message {
      name 'int32'
    }

    dsl.service {
      name "formService"
      post "/form" spec {
        body form('FormType')
        response 'int32'
      }
    }

    then:
    dsl.serviceByName("formService").methods.first().body instanceof FormType

  }

  def "can handle form body with closure"() {
    when:
    dsl.type 'int32'

    dsl.service {
      name "formService"
      post "/form" spec {
        body form {
          name 'int32'
        }
        response 'int32'
      }
    }

    then:
    dsl.serviceByName("formService").methods.first().body instanceof FormType
  }

  def "should only allow messages as form types"() {
    when:
    dsl.type 'int32'

    dsl.service {
      name 'formService'
      put "/form" spec {
        body form('int32')
        response 'int32'
      }
    }

    then:
    thrown(IllegalArgumentException)
  }

  def "should not allow nested messages"() {
    when:
    dsl.type 'int32'
    dsl.type "SmallMessage" message {
      count 'int32'
    }
    dsl.type "BigMessage" message {
      small "SmallMessage"
    }
    dsl.service {
      name "formService"
      post "/form" spec {
        body form("BigMessage")
      }
    }

    then:
    thrown IllegalArgumentException
  }

  def "can set data type as body"() {
    when:
    dsl.type 'int32'

    dsl.service {
      name "Raw Data Service"

      post "/data" spec {
        response 'int32'
        body data()
      }
    }

    then:
    dsl.serviceByName("Raw Data Service").methods.first().body instanceof DataType
  }

  def "can set multipart type as body"() {
    when:
    dsl.type 'int32'

    dsl.service {
      name 'Multipart Service'

      post '/parts' spec {
        response 'int32'
        body multipart()
      }
    }

    then:
    dsl.serviceByName("Multipart Service").methods.first().body instanceof MultipartType
  }

  def "can set multipart with map as body"() {
    when:
    dsl.type 'int32'
    dsl.type 'string'
    dsl.type 'Person' message {
      name 'string'
      age  'int32'
    }

    dsl.service {
      name 'Upload parts'

      post '/upload' spec {
        response 'int32'
        body multipart {
          title  'string'
          number 'int32'
          person 'Person'
          file1  file()
          data1  data()
        }
      }
    }
    Type body = dsl.serviceByName("Upload parts").methods.first().body

    then:
    body instanceof MultipartType
    (body as MultipartType).parts["title"].name == 'string'
    (body as MultipartType).parts["number"].name == 'int32'
    (body as MultipartType).parts["person"] instanceof Message

    ((body as MultipartType).parts["person"] as Message).name == 'Person'
    ((body as MultipartType).parts["person"] as Message).fieldByName("name").type.name == 'string'
    ((body as MultipartType).parts["person"] as Message).fieldByName('age').type.name == 'int32'

    (body as MultipartType).parts["file1"] instanceof FileType
    (body as MultipartType).parts["data1"] instanceof DataType
  }

  def "default multipart type is mixed"() {
    when:
    dsl.service {
      name 'Upload parts'

      post '/upload' spec {
        body multipart()
      }
    }
    Type body = dsl.serviceByName("Upload parts").methods.first().body

    then:
    body instanceof MultipartType
    (body as MultipartType).subtype == MultipartType.Subtype.MIXED
  }

  def "can set allowed multipart types"() {
    when:
    for (MultipartType.Subtype type in MultipartType.Subtype.values()) {
      dsl.service {
        name 'Upload parts'

        post '/upload' spec {
          body multipart("${type.representation()}")
        }
      }
      Type body = dsl.serviceByName("Upload parts").methods.first().body
    }
    then:
    notThrown(IllegalArgumentException)
  }

  def "can detect wrong multipart type"() {
    when:
    dsl.service {
      name 'Upload parts'

      post '/upload' spec {
        body multipart("abracadabra")
     }
    }

    then:
    def ex = thrown IllegalArgumentException
    ex.message.contains "Bad content type of multipart body"

  }

  def "can set multipart with type and closure"() {
    when:
    dsl.type 'int32'
    dsl.type 'string'
    dsl.type 'Person' message {
      name 'string'
      age  'int32'
    }

    dsl.service {
      name 'Upload parts'

      post '/upload' spec {
        response 'int32'
        body multipart("form-data") {
          title  'string'
          number 'int32'
          person 'Person'
          file1  file()
          data1  data()
        }
      }
    }
    Type body = dsl.serviceByName("Upload parts").methods.first().body

    then:
    body instanceof MultipartType
    (body as MultipartType).subtype == MultipartType.Subtype.FORM_DATA
    (body as MultipartType).parts["title"].name == 'string'
    (body as MultipartType).parts["number"].name == 'int32'
    (body as MultipartType).parts["person"] instanceof Message

    ((body as MultipartType).parts["person"] as Message).name == 'Person'
    ((body as MultipartType).parts["person"] as Message).fieldByName("name").type.name == 'string'
    ((body as MultipartType).parts["person"] as Message).fieldByName('age').type.name == 'int32'

    (body as MultipartType).parts["file1"] instanceof FileType
    (body as MultipartType).parts["data1"] instanceof DataType

  }

  //endregion

  //region oauth2 authentication

  def "can define oauth2 client credentials auth"() {
    when:
    dsl.service {
      name 'Some service'
      authentication oauth2()
      tests {
        oauth2 {
          type 'client_credentials'
          tokenUrl 'https://url'
          clientId 'id'
          clientSecret 'secret'
        }
      }
    }
    def service = dsl.serviceByName('Some service')

    then:
    service.authenticationTypes() == [Authentication.Type.OAUTH2]
    service.testInfo.authParams instanceof Oauth2AuthenticationParams
    ((Oauth2AuthenticationParams) service.testInfo.authParams).type == Oauth2AuthenticationParams.AuthType.CLIENT_CREDENTIALS
    ((Oauth2AuthenticationParams) service.testInfo.authParams).tokenUrl == 'https://url'
    ((Oauth2AuthenticationParams) service.testInfo.authParams).clientId == 'id'
    ((Oauth2AuthenticationParams) service.testInfo.authParams).clientSecret == 'secret'
  }

  //endregion

}
