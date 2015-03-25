package com.stanfy.helium.handler.codegen.tests

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import com.stanfy.helium.dsl.ProjectDsl
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

  private static final String UPLOAD_FORM_SCENARIO = "upload Kapitoshka form"

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

      tests {
        scenario "$UPLOAD_FORM_SCENARIO" spec {
          def res = post '/upload' with {
            body form {
              name 'Kapitoshka'
              age  '3'
            }
          }
        }
      }
    }

    //endregion

    executor = new HttpExecutor(project.types, new OkHttpClient())
  }

  def cleanup() {
    mockWebServer.shutdown()
  }

  def "should write form body"() {
    when:
    executeScenario(UPLOAD_FORM_SCENARIO)
    def sent = mockWebServer.takeRequest()
    String receivedBody = sent.body.readUtf8()

    then:
    receivedBody != null
    receivedBody.contains 'name=Kapitoshka&age=3'
  }

  private void executeScenario(final String name) {
    Service service = project.serviceByName(SERVICE_NAME)
    ScenarioInvoker.invokeScenario(new ScenarioDelegate(service, executor), service.testInfo.scenarioByName(name))
  }
}
