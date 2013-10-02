package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
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
            parameters {
              testParam false
            }
            body {
              f1 true
            }
          }
        }
      }

    }

    service = dsl.services[0]
    executor = new Executor()
    delegate = new ScenarioDelegate(service, executor)

  }

  def "can execute service methods"() {
    when:
    def action = service.testInfo.scenarios[0].action
    action.delegate = delegate
    action.resolveStrategy = Closure.DELEGATE_FIRST
    action()

    then:
    executor.executedMethods[0].path == "some/resource/@id"
    executor.executedMethods[0].type == MethodType.POST
  }

  /** Executor instance. */
  private static class Executor implements ScenarioExecutor {

    /** List of executed methods. */
    List<ServiceMethod> executedMethods = []

    @Override
    Object performMethod(final Service service, final ServiceMethod method) {
      assert service != null
      executedMethods += method
    }

  }

}
