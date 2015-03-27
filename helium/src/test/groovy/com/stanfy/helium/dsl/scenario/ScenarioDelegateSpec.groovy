package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.entities.ByteArrayEntity
import com.stanfy.helium.model.DataType
import com.stanfy.helium.model.FormType
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.MultipartType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.tests.Scenario
import com.stanfy.helium.utils.ConfigurableMap
import spock.lang.Specification

/**
 * Spec for ScenarioDelegate.
 */
class ScenarioDelegateSpec extends Specification {

  /** Instance. */
  ScenarioDelegate delegate

  /** Executor. */
  Executor executor

  /** Service. */
  Service service

  def setup() {

    //region prepare service

    ProjectDsl dsl = new ProjectDsl()
    dsl.type 'bool'
    dsl.type 'string'
    dsl.type 'Msg' message {
      f1 'bool'
    }
    dsl.service {
      name "Test service"

      MethodType.values().each { MethodType type ->
        "${type.toString().toLowerCase(Locale.US)}" "some/resource/@id" spec {
          name "test method $type"
          parameters {
            testParam 'bool' optional
          }
          response 'Msg'
          if (type.hasBody) {
            body 'Msg'
          }
        }
      }

      get "/headers/example" spec {
        httpHeaders header('H1', 'v1'), 'H2'
        response 'Msg'
      }

      post "/upload_form" spec {
        response 'Msg'
        body form {
          checked 'bool'
          name    'string'
        }
      }

      post "/upload_bytes" spec {
        response 'Msg'
        body data()
      }

      post "/upload_multipart" spec {
        body multipart {
          name           'string'
          my_message     'Msg'
          inline_message 'Msg'
          file1          file()
          some_data      data()
        }
      }

      tests {

        scenario "simple post" spec {
          post "some/resource/@id" with {
            path {
              id "222"
            }
            httpHeaders {
              "H1" "V1"
            }
            parameters {
              testParam false
            }
            body {
              f1 true
            }
          }
        }
        scenario "get and assert 'ok'" spec {
          def someRes = get "some/resource/@id" with {
            path {
              id '1'
            }
          }
          assert someRes.statusCode == 200
          assert someRes.body == "ok" : "Result is incorrect: $someRes"
          return someRes
        }
        scenario "delete and assert 'hi'" spec {
          def someRes = delete "some/resource/@id" with {
            path {
              id '2'
            }
          }
          assert someRes.body == "hi" : "Result is incorrect: $someRes"
          return someRes
        }

        def someBefore = {
          store "NAME", "VALUE"
          store "T", true
        }

        def afterStore = {
          assert NAME == "VALUE"
          assert T
        }

        scenario "check store", before: someBefore, after: afterStore spec {
          assert NAME == "VALUE"
          post "some/resource/@id" with {
            path {
              id NAME
            }
            httpHeaders {
              "H1" NAME
            }
            parameters {
              testParam T
            }
            body {
              f1 T
            }
          }
        }

        scenario "inside closures" spec {
          ['1', '2', '3'].each { idValue ->
            def result = get "some/resource/@id" with {
              path { id idValue }
            }
            assert result.body == "passed"
          }
        }

        scenario "report problems" spec {
          report "start"
          5.times { report it }
          report new IllegalStateException("end")
        }

        scenario "unknown variable used" spec {
          store 'key', badUnknownVariableName
        }

        scenario "resolved header" spec {
          def test = get "/headers/example" with {
            httpHeaders { 'H2' 'v2' }
          }
        }

        scenario "bad call syntax" spec {
          def failed = get "/some/resourceIncorrectUri/@id" {
            path { id "test" }
          }
        }

        scenario "bad call syntax 2" spec {
          def failed = get "/some/resource/@id" {
            path { id "test" }
          }
        }

        scenario "upload form" spec {
          def result = post '/upload_form' with {
            body form {
              checked true
              name    'Request'
            }
          }
          result.mustSucceed()
        }

        scenario "upload bytes" spec {
          def strBytes = "Happy bytes string".getBytes()
          def result = post "/upload_bytes" with {
            body bytes(strBytes as byte[])
          }

          result.mustSucceed()
        }

        scenario 'upload multipart' spec {
          def myName = "Kapitoshka"

          File testFile = new File("testfile.txt")
          FileOutputStream stream = new FileOutputStream(testFile)
          stream.write("This sentence should be in the file.".getBytes())
          stream.close()
          testFile.deleteOnExit()

          def someBytes = 'generic bytes data'.getBytes()

          def resp = post "/upload_multipart" with {
            body multipart {
              name myName
              inline_message {
                f1 true
              }
              file1 testFile
              some_data someBytes
            }
          }
          resp.mustSucceed()
        }
      }

    }
    //endregion

    service = dsl.services[0]
    executor = new Executor()
    delegate = new ScenarioDelegate(service, executor)

  }

  private Object executeScenario(final String name, final def result, final def errors) {
    executor.scheduledExecutorResult = new ExecResult(
        body: result,
        interactionErrors: errors ? errors : [],
        willSucceed: true,
        statusCode: 200
    )
    Scenario scenario = service.testInfo.scenarioByName(name)
    return ScenarioInvoker.invokeScenario(delegate, scenario)
  }

  def "can execute service methods"() {
    when:
    def res = executeScenario("simple post", "ok", null)

    then:
    res.body == "ok"

    !executor.executedMethods.empty
    executor.executedMethods[0].path == "/some/resource/@id"
    executor.executedMethods[0].type == MethodType.POST

    !executor.requests.empty
    executor.requests[0].pathParameters['id'] == '222'
    executor.requests[0].httpHeaders['H1'] == 'V1'
    executor.requests[0].parameters.value.testParam == false
    executor.requests[0].body.value.f1 == true
  }

  def "asserts work with executor results"() {
    when:
    def res1 = executeScenario("get and assert 'ok'", "ok", null)
    def res2 = executeScenario("delete and assert 'hi'", "ok", null)

    then:
    def e = thrown(AssertionError)
    e.message.contains("Result is incorrect")
    res1 != null
    res1.body == "ok"
    res2 == null
    executor.executedMethods[0].type == MethodType.GET
    executor.requests[0].pathParameters['id'] == '1'
    executor.executedMethods[1].type == MethodType.DELETE
    executor.requests[1].pathParameters['id'] == '2'
  }

  def "store works great"() {
    when:
    executeScenario("check store", "1", null)

    then:
    executor.executedMethods[0].type == MethodType.POST
    !executor.requests.empty
    executor.requests[0].pathParameters['id'] == 'VALUE'
    executor.requests[0].httpHeaders['H1'] == 'VALUE'
    executor.requests[0].parameters.value.testParam == true
    executor.requests[0].body.value.f1 == true
  }

  def "caught result interaction errors are reported"() {
    when:
    executeScenario("check store", "1", [new AssertionError("bla bla bla")])

    then:
    def e = thrown(AssertionError)
    e.message.contains("bla bla bla")
  }

  def "we can call methods inside closures"() {
    when:
    executeScenario("inside closures", "passed", null)
    then:
    executor.executedMethods.last().type == MethodType.GET
  }

  def "we can report about problems"() {
    when:
    executeScenario("report problems", null, null)
    then:
    def e = thrown(AssertionError)
    e.message.contains("start")
    e.message.contains("end")
    e.message.contains("2")
    e.message.contains("3")
  }

  def "unknown variable names are correctly reported"() {
    when:
    executeScenario("unknown variable used", null, null)
    then:
    def e = thrown(AssertionError)
    e.message.contains("badUnknownVariableName")
    e.message.contains("not defined")
  }

  def "headers should be resolved"() {
    when:
    executeScenario("resolved header", "ok", null)
    then:
    executor.executedMethods.size() == 1
  }

  def "meaningful message for bad uri and missed 'with'"() {
    when:
    executeScenario("bad call syntax", null, null)
    then:
    def e = thrown(AssertionError)
    e.message.contains("'with'")
    e.message.contains("/some/resourceIncorrectUri/@id")
  }

  def "meaningful message for missed 'with'"() {
    when:
    executeScenario("bad call syntax 2", null, null)
    then:
    def e = thrown(AssertionError)
    e.message.contains("'with'")
    e.message.contains("/some/resource/@id")
  }

  def "form data is parsed"() {
    when:
    executeScenario("upload form", null, null)

    then:
    executor.executedMethods.size() == 1
    executor.requests.size() == 1
    executor.requests.first().body.type instanceof FormType
    executor.requests.first().body.value instanceof Map
    (executor.requests.first().body.value as Map).checked == true
    (executor.requests.first().body.value as Map).name == 'Request'
  }

  def "generic data body is parsed"() {
    when:
    executeScenario("upload bytes", null, null)

    then:
    executor.executedMethods.size() == 1
    executor.requests.first().body.type instanceof DataType
    executor.requests.first().body.value instanceof ByteArrayEntity
    (executor.requests.first().body.value as ByteArrayEntity).bytes == "Happy bytes string".getBytes()
  }

  def "multipart data body is parsed"() {
    when:
    executeScenario("upload multipart", null, null,)

    then:
    executor.executedMethods.size() == 1
    executor.requests.first().body.type instanceof MultipartType
    executor.requests.first().body.value instanceof Map<String, Object>
    (executor.requests.first().body.value as Map<String, Object>).name == 'Kapitoshka'

    (executor.requests.first().body.value as Map<String, Object>).file1 instanceof File
    ((executor.requests.first().body.value as Map<String, Object>).file1 as File).text.contains "This sentence should be in the file."

    (executor.requests.first().body.value as Map<String, Object>).some_data instanceof ByteArrayEntity
    ((executor.requests.first().body.value as Map<String, Object>).some_data as ByteArrayEntity).bytes == 'generic bytes data'.getBytes()


  }

  /** Executor instance. */
  private static class Executor implements ScenarioExecutor {

    /** List of executed methods. */
    List<ServiceMethod> executedMethods = []
    /** List of executed requests. */
    List<ServiceMethodRequestValues> requests = []

    ExecResult scheduledExecutorResult = null

    @Override
    ExecResult performMethod(final Service service, final ServiceMethod method, final ServiceMethodRequestValues request) {
      assert service != null
      if (method) {
        executedMethods += method
      }
      if (request) {
        requests += request
      }
      return scheduledExecutorResult
    }

  }

  /** Test result. */
  private static class ExecResult implements MethodExecutionResult {
    def body
    List<AssertionError> interactionErrors

    boolean willSucceed

    def headers = [:]

    int statusCode

    @Override
    Map<String, String> getHttpHeaders() {
      return headers
    }

    @Override
    void mustSucceed() {
      if (!willSucceed) {
        throw new AssertionError("test")
      }
    }

    @Override
    void mustBeClientError() {
      if (!willSucceed) {
        throw new AssertionError("test")
      }
    }

    @Override
    boolean isSuccessful() {
      return willSucceed
    }

  }

}
