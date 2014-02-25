package com.stanfy.helium.dsl

import com.stanfy.helium.dsl.scenario.ScenarioDelegate
import com.stanfy.helium.dsl.scenario.ScenarioInvoker
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.tests.ServiceTestInfo
import com.stanfy.helium.utils.ConfigurableProxy
import groovy.transform.CompileStatic

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Extended proxy for services configuration.
 */
class ConfigurableService extends ConfigurableProxy<Service> {

  static {
    MethodType.values().each { MethodType type ->
      ConfigurableService.metaClass."${type.name}" << { Object arg ->

        // this delegate methods overlap with ScenarioDelegate
        // here we check actual stacktrace to ensure that this method is not invoked from a scenario
        ScenarioDelegate scenarioDelegate = ScenarioInvoker.getDelegate()
        if (scenarioDelegate) {
          return scenarioDelegate."${type.name}"(arg)
        }

        String path = "$arg"
        return [
            "spec" : { Closure<?> spec -> delegate.addServiceMethod(path, type, spec) }
        ]
      }
    }
  }

  ConfigurableService(final Service core, final ProjectDsl project) {
    super(core, project)
  }

  @CompileStatic
  ServiceMethod addServiceMethod(final String path, final MethodType type, Closure<?> spec) {
    ServiceMethod method = new ServiceMethod(path: path, type: type)

    Closure<?> body = spec.clone() as Closure<?>
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = new ConfigurableServiceMethod(method, getProject())
    body.call()

    if (!method.path) {
      throw new IllegalStateException("Path is not defined for service method $method in '${getCore().name}'")
    }
    if (!method.type) {
      throw new IllegalStateException("Type is not defined for service method $method in '${getCore().name}'")
    }

    getCore().methods.add method
    return method
  }

  @CompileStatic
  ServiceTestInfo tests(final Closure<?> spec) {
    Service service = getCore()
    runWithProxy(new ConfigurableServiceTestInfo(service.testInfo, getProject()), spec)
    return service.testInfo
  }

}
