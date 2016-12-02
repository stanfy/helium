package com.stanfy.helium.internal.dsl

import com.stanfy.helium.internal.dsl.scenario.ScenarioDelegate
import com.stanfy.helium.internal.dsl.scenario.ScenarioInvoker
import com.stanfy.helium.internal.model.tests.BehaviourDescription
import com.stanfy.helium.internal.model.tests.CheckBuilder
import com.stanfy.helium.internal.model.tests.CheckableService
import com.stanfy.helium.internal.utils.ConfigurableProxy
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Authentication
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.tests.ServiceTestInfo
import groovy.transform.CompileStatic

import static com.stanfy.helium.internal.utils.DslUtils.runWithProxy

/**
 * Extended proxy for services configuration.
 */
class ConfigurableService extends ConfigurableProxy<CheckableService> {

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
    Authentication.Type.values().each { Authentication.Type type ->
      ConfigurableService.metaClass."${type.getName()}" << { Map args = null, Closure<?> config = null ->
        return delegate.createSecurity(args, config, type)
      }
      ConfigurableService.metaClass."${type.getName()}" << { Closure<?> config ->
        return delegate.createSecurity(null, config, type)
      }
    }
  }

  ConfigurableService(final CheckableService core, final ProjectDsl project) {
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

    def methods = getCore().methods
    if (methods.contains(method)) {
      throw new IllegalStateException("Method $method.type $method.path is already declared")
    }
    methods.add method
    return method
  }

  @CompileStatic
  ServiceTestInfo tests(final Closure<?> spec) {
    Service service = getCore()
    runWithProxy(new ConfigurableServiceTestInfo(service.testInfo, getProject()), spec)
    return service.testInfo
  }

  @CompileStatic
  BehaviourDescriptionBuilder describe(final String name) {
    CheckBuilder builder = BehaviourDescription.currentBuilder()
    return builder != null ? builder.describe(name) : new BehaviourDescriptionBuilder(name, getCore(), getProject())
  }

  void authentication(Authentication auth) {
    getCore().authentications.add auth
  }

  Authentication createSecurity(Map args, Closure<?> config, Authentication.Type type) {
    Authentication auth = args ? new Authentication(args) : new Authentication()
    if (config) {
      runWithProxy(new ConfigurableProxy<Authentication>(auth, getProject()), config)
    }
    auth.type = type
    auth.name = type.name
    return auth
  }

}
