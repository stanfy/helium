package com.stanfy.helium.handler.tests

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import com.stanfy.helium.internal.dsl.DefaultTypeResolver
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.tests.BehaviourCheck
import com.stanfy.helium.model.tests.CheckListener
import spock.lang.Specification

class HttpExecutorAuthSpec extends Specification {

  private MockWebServer server
  private ProjectDsl dsl
  private HttpExecutor executor

  def setup() {
    executor = new HttpExecutor(new DefaultTypeResolver(), new OkHttpClient())
    server = new MockWebServer()

    def contentType = "application/vnd.api+json"

    server.enqueue(new MockResponse()
        .setResponseCode(401)
        .setBody("{\"errors\":{\"detail\":\"Missing an Authorization request header.\"}}")
        .addHeader("Content-type: $contentType")
    )
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"scope\": \"public\", \"token_type\": \"Bearer\", \"expires_in\": 36000, \"access_token\": \"access-token\"}")
        .addHeader("Content-type: $contentType")
    )
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("ok")
        .addHeader("Content-type: $contentType")
    )

    server.start()

    dsl = new ProjectDsl()
    this.dsl.service {
      name 'Test service'
      location "${server.getUrl('/')}"

      get '/test' spec { }

      tests {
        httpHeaders {
          'Content-Type' contentType
        }
        oauth2 {
          type 'client_credentials'
          tokenUrl "${server.getUrl('/token')}"
          clientId 'some-id'
          clientSecret 'some-secret'
        }
      }

      describe "some test" spec {
        it("must pass") {
          def resp = service('Test service').get '/test' with { }
          assert resp.statusCode == 200
        }
      }
    }
  }

  def "integration test"() {
    when:
    def checks = dsl.services.first().check(executor, Mock(CheckListener))

    then:
    checks.result == BehaviourCheck.Result.PASSED
    server.requestCount == 3

    def firstRequest = server.takeRequest()
    firstRequest.path == '/test'
    !firstRequest.headers.names().contains('Authorization')
    firstRequest.headers.get('Content-Type') == 'application/vnd.api+json'

    server.takeRequest().path.startsWith('/token')

    def lastRequest = server.takeRequest()
    lastRequest.path == '/test'
    lastRequest.headers.get('Authorization') == 'Bearer access-token'
    lastRequest.headers.get('Content-Type') == 'application/vnd.api+json'
  }

  def tearDown() {
    server.shutdown()
  }

}
