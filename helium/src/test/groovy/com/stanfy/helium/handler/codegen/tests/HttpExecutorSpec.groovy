package com.stanfy.helium.handler.codegen.tests

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import com.squareup.okhttp.mockwebserver.RecordedRequest
import com.stanfy.helium.handler.tests.HttpExecutor
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.internal.dsl.scenario.ScenarioDelegate
import com.stanfy.helium.internal.dsl.scenario.ScenarioInvoker
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.tests.BehaviourCheck
import com.stanfy.helium.model.tests.CheckListener
import spock.lang.Specification

/**
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
class HttpExecutorSpec extends Specification {

  MockWebServer mockWebServer
  ProjectDsl project
  HttpExecutor executor

  private static final String SERVICE_NAME = "Service for uploads"

  def setup() {
    mockWebServer = new MockWebServer()
    mockWebServer.enqueue(new MockResponse().setBody("OK"))
    mockWebServer.start()

    //region Project DSL setup.

    project = new ProjectDsl()
    project.type 'int32'
    project.type 'string'
    project.service {
      name "$SERVICE_NAME"

      location "${mockWebServer.getUrl('/')}"

      post '/upload' spec {
        body form {
          name 'string'
          age  'int32'
        }
      }

      post '/upload_bytes' spec {
        body data()
      }

      post '/upload_multipart' spec {
        body multipart('form-data') {
          name 'string'
          dragon_bytes data()
        }
      }

      tests {
        scenario "upload Kapitoshka form" spec {
          def res = post '/upload' with {
            body form {
              name 'Kapitoshka'
              age  '3'
            }
          }
        }

        scenario "upload bytes string" spec {
          def uploadBytes = "Hello. Here be dragons !".getBytes()
          def res = post '/upload_bytes' with {
            body bytes(uploadBytes)
          }
        }

        scenario "upload multipart form" spec {
          def dragonName = "Dragon!!!"
          def theBytes = "1234567890".getBytes()
          def res = post "/upload_multipart" with {
            body multipart {
              name dragonName
              dragon_bytes theBytes
            }
          }
          res.mustSucceed()
        }
      }
    }

    //endregion

    executor = new HttpExecutor(project.types, new OkHttpClient())
  }

  def cleanup() {
    mockWebServer.shutdown()
  }

  def "should simple write form body"() {
    when:
    def sent = executeScenario("upload Kapitoshka form")
    String receivedBody = sent.body.readUtf8()

    then:
    receivedBody != null
    receivedBody.contains 'name=Kapitoshka&age=3'
  }

  def "should write simple bytes as generic body"() {
    given:
    def bytesToUpload = "Hello. Here be dragons !".getBytes()

    when:
    def sent = executeScenario("upload bytes string")
    def receivedBytes = sent.body.readByteArray()

    then:
    Arrays.equals(bytesToUpload, receivedBytes)
  }

  def "should upload multipart body"() {
    when:
    def sent = executeScenario("upload multipart form")
    String receivedBody = sent.body.readUtf8()
    println("Received body:\n" + receivedBody)

    then:
    println("Recieved headers: " + sent.headers)
    sent.getHeader("Content-Type").startsWith "multipart/form-data"
    receivedBody != null
    receivedBody.contains "Dragon!!!"
    receivedBody.contains "1234567890"
  }

  private def executeScenario(final String name) {
    Service service = project.serviceByName(SERVICE_NAME)
    ScenarioInvoker.invokeScenario(new ScenarioDelegate(service, executor), service.testInfo.scenarioByName(name))
    return mockWebServer.takeRequest()
  }

  def "authentication"() {
    given:
    project = new ProjectDsl()
    project.type 'int32'
    project.type 'string'
    project.service {
      name 'test'
      authentication oauth2()
      location mockWebServer.getUrl('/')

      get '/test' spec {
        name 'Test request'
      }

      tests {
        oauth2 {
          type 'client_credentials'
          tokenUrl mockWebServer.getUrl("/oauth/token")
          clientId 'id'
          clientSecret 'secret'
        }
      }

      describe 'Some test' spec {
        it('works') {
          def resp = service.get '/test' with { }
          assert resp.statusCode == 200
        }
      }
    }

    and:
    def queue = mockWebServer.dispatcher
    mockWebServer.dispatcher = { RecordedRequest request ->
      def response = new MockResponse()
      if (request.path.startsWith("/oauth/token")) {
        return response.setBody("{\"access_token\": \"val\"}")
      }
      if (!request.headers.get("Authorization")) {
        return response.setResponseCode(401)
      }
      return queue.dispatch(request)
    } as Dispatcher

    when:
    def res = project.serviceByName('test').check(executor, Mock(CheckListener))

    then:
    res.result == BehaviourCheck.Result.PASSED
    mockWebServer.takeRequest().path == '/test'
    mockWebServer.takeRequest().path.startsWith('/oauth/token')
    def lastRequest = mockWebServer.takeRequest()
    lastRequest.path == '/test'
    lastRequest.headers.get('Authorization') == 'Bearer val'
  }

}
