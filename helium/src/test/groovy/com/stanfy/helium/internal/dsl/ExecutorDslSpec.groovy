package com.stanfy.helium.internal.dsl

import com.stanfy.helium.Helium
import com.stanfy.helium.dsl.MethodExecutionResult
import com.stanfy.helium.internal.MethodsExecutor
import com.stanfy.helium.internal.ServiceMethodRequestValues
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import spock.lang.Specification

/**
 * A set of tests for ExecutorDSL in checks.
 */
class ExecutorDslSpec extends Specification {

  ProjectDsl project
  Executor executor

  def setup() {
    project = new Helium().defaultTypes().project as ProjectDsl
    executor = new Executor()
  }

  private ExecutorDsl dsl(final String serviceName) {
    return new ExecutorDsl(project.serviceByName(serviceName), executor)
  }

  def "can execute get service methods"() {
    given:
    project.service {
      name "test"

      get "/@id" spec {
        response 'string'
      }
    }
    def dsl = dsl("test")

    when:
    executor.scheduledExecutorResults += new ExecResult(willSucceed: true, body: "ok")
    def getResp = dsl.get "/@id" with { path { id 123 } }

    then:
    getResp.mustSucceed()
    getResp.body == "ok"
    executor.requests[0]?.pathParameters?.id == "123"
    executor.executedMethods[0]?.type == MethodType.GET
  }

  def "can execute post service methods"() {
    given:
    project.service {
      name "test"

      post "/@id" spec {
        body 'string'
        response 'int32'
      }
    }
    def dsl = dsl("test")

    when:
    executor.scheduledExecutorResults += new ExecResult(willSucceed: true, body: 2)
    def postResp = dsl.post "/@id" with { body 'body string'; path { id "my-id" } }

    then:
    postResp.mustSucceed()
    postResp.body == 2
    executor.requests[0]?.pathParameters?.id == "my-id"
    executor.requests[0]?.body?.type?.name == 'string'
    executor.requests[0]?.body?.value == 'body string'
    executor.executedMethods[0]?.type == MethodType.POST
  }

  def "can execute put service methods"() {
    given:
    project.service {
      name "test"

      put "/tst" spec {
        response 'int64'
      }
    }
    def dsl = dsl("test")

    when:
    executor.scheduledExecutorResults += new ExecResult(willSucceed: true, body: 56L)
    def putResp = dsl.put "/tst" with { }

    then:
    putResp.mustSucceed()
    executor.executedMethods[0]?.type == MethodType.PUT
    putResp.body == 56
  }

  def "can execute delete service methods"() {
    given:
    project.service {
      name "test"

      delete "/tst" spec { }
    }
    def dsl = dsl("test")

    when:
    executor.scheduledExecutorResults += new ExecResult(willSucceed: true)
    def putResp = dsl.delete "/tst" with { }

    then:
    putResp.mustSucceed()
    executor.executedMethods[0]?.type == MethodType.DELETE
  }

  def "can path all params"() {
    given:
    project.type "Foo" message {
      bar 'string'
    }
    project.service {
      name "params test"

      put "/@id/name" spec {
        httpHeaders "h1", "h2", header("h3", "v3")
        body "Foo"
        parameters {
          "p1" "string"
        }
      }
    }
    def dsl = dsl("params test")

    when:
    def resp = dsl.put "/@id/name" with {
      httpHeaders {
        "h1" "v1"
        "h2" "v2"
      }
      body {
        bar "gamma"
      }
      parameters {
        p1 "q"
      }
      path {
        id 123
      }
    }

    then:
    executor.executedMethods[0]?.type == MethodType.PUT
    executor.requests[0]?.pathParameters?.id == "123"
    executor.requests[0]?.body?.value?.bar == "gamma"
    executor.requests[0]?.body?.type?.name == "Foo"
    executor.requests[0]?.httpHeaders?.size() == 2
    executor.requests[0]?.parameters?.value?.p1 == "q"
  }

  /** Executor instance. */
  private static class Executor implements MethodsExecutor {

    /** List of executed methods. */
    List<ServiceMethod> executedMethods = []
    /** List of executed requests. */
    List<ServiceMethodRequestValues> requests = []

    List<ExecResult> scheduledExecutorResults = []

    @Override
    ExecResult performMethod(final Service service, final ServiceMethod method, final ServiceMethodRequestValues request) {
      assert service != null
      assert method != null
      assert request != null
      requests += request
      executedMethods += method
      return scheduledExecutorResults.empty ? null : scheduledExecutorResults.pop()
    }

  }

  /** Test result. */
  private static class ExecResult implements MethodExecutionResult {
    def body

    boolean willSucceed

    Map<String, String> httpHeaders = [:]

    int statusCode

    @Override
    void mustSucceed() {
      if (!willSucceed) {
        throw new AssertionError("mustSucceed")
      }
    }

    @Override
    void mustBeClientError() {
      if (!willSucceed) {
        throw new AssertionError("mustBeClientError")
      }
    }

    @Override
    boolean isSuccessful() {
      return willSucceed
    }

    @Override
    List<AssertionError> getInteractionErrors() {
      return []
    }
  }

}
