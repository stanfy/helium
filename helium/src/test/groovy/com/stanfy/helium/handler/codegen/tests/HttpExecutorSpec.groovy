package com.stanfy.helium.handler.codegen.tests

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.dsl.scenario.ScenarioDelegate
import com.stanfy.helium.dsl.scenario.ScenarioInvoker
import com.stanfy.helium.model.Service
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
        body multipart('form') {
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
          def dragon_name = "Dragon!!!"
          def theBytes = "1234567890".getBytes()
          def res = post "/upload_multipart" with {
            body multipart {
              name dragon_name
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
    sent.getHeader("Content-Type").contains "multipart/form"
    receivedBody != null
    receivedBody.contains "Dragon!!!"
    receivedBody.contains "1234567890"
  }

  private def executeScenario(final String name) {
    Service service = project.serviceByName(SERVICE_NAME)
    ScenarioInvoker.invokeScenario(new ScenarioDelegate(service, executor), service.testInfo.scenarioByName(name))
    return mockWebServer.takeRequest()
  }
}
