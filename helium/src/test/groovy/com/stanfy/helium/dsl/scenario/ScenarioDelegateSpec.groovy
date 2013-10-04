package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.tests.Scenario
import com.stanfy.helium.utils.DslUtils
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

    // prepare service
    ProjectDsl dsl = new ProjectDsl()
    dsl.type 'bool'
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
          assert someRes == "ok" : "Result is incorrect: $someRes"
          return someRes
        }
        scenario "delete and assert 'hi'" spec {
          def someRes = delete "some/resource/@id" with {
            path {
              id '2'
            }
          }
          assert someRes == "hi" : "Result is incorrect: $someRes"
          return someRes
        }

        def someBefore = {
          store "NAME", "VALUE"
          store "T", true
        }

        scenario "check store", before: someBefore spec {
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

      }

    }

    service = dsl.services[0]
    executor = new Executor()
    delegate = new ScenarioDelegate(service, executor)

  }

  private def executeScenario(final String name, final def result) {
    executor.scheduledExecutorResult = result
    Scenario scenario = service.testInfo.scenarioByName(name)
    if (scenario.before) {
      DslUtils.runWithProxy(delegate, scenario.before)
    }
    return DslUtils.runWithProxy(delegate, scenario.action)
  }

  def "can execute service methods"() {
    when:
    def res = executeScenario("simple post", "ok")

    then:
    res == "ok"

    !executor.executedMethods.empty
    executor.executedMethods[0].path == "some/resource/@id"
    executor.executedMethods[0].type == MethodType.POST

    !executor.requests.empty
    executor.requests[0].pathParameters['id'] == '222'
    executor.requests[0].httpHeaders['H1'] == 'V1'
    executor.requests[0].parameters.value.testParam == false
    executor.requests[0].body.value.f1 == true
  }

  def "asserts work with executor results"() {
    when:
    def res1 = executeScenario("get and assert 'ok'", "ok")
    def res2 = executeScenario("delete and assert 'hi'", "ok")

    then:
    def e = thrown(AssertionError)
    e.message.contains("Result is incorrect")
    res1 == "ok"
    res2 == null
    executor.executedMethods[0].type == MethodType.GET
    executor.requests[0].pathParameters['id'] == '1'
    executor.executedMethods[1].type == MethodType.DELETE
    executor.requests[1].pathParameters['id'] == '2'
  }

  def "store works great"() {
    when:
    executeScenario("check store", "1")

    then:
    executor.executedMethods[0].type == MethodType.POST
    !executor.requests.empty
    executor.requests[0].pathParameters['id'] == 'VALUE'
    executor.requests[0].httpHeaders['H1'] == 'VALUE'
    executor.requests[0].parameters.value.testParam == true
    executor.requests[0].body.value.f1 == true
  }

  /** Executor instance. */
  private static class Executor implements ScenarioExecutor {

    /** List of executed methods. */
    List<ServiceMethod> executedMethods = []
    /** List of executed requests. */
    List<ServiceMethodRequestValues> requests = []

    def scheduledExecutorResult = null


    @Override
    Object performMethod(final Service service, final ServiceMethod method, final ServiceMethodRequestValues request) {
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

}
