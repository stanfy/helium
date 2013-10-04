package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.entities.TypedEntity
import com.stanfy.helium.entities.TypedEntityValueBuilder
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.Type
import com.stanfy.helium.utils.ConfigurableStringMap
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * Scenario closure delegate.
 */
class ScenarioDelegate {

  static {
    MethodType.values().each { MethodType type ->
      ScenarioDelegate.metaClass."${type.name}" << { String path ->
        ScenarioDelegate sd = delegate as ScenarioDelegate
        return sd.prepareMethodExecution(type, path)
      }
    }
  }

  /** Underlying service. */
  final Service service

  /** Executor. */
  final ScenarioExecutor executor

  /** Variables scope. */
  // TODO: tests for scopes
  private final LinkedHashMap<String, Object> scope = new LinkedHashMap<>()

  public ScenarioDelegate(final Service service, final ScenarioExecutor executor) {
    this.service = service
    this.executor = executor
  }

  public void store(final String name, final Object value) {
    scope.put(name, value)
  }

  @CompileStatic
  private Object prepareMethodExecution(final MethodType type, final String path) {
    ServiceMethod method = service.methods.find { ServiceMethod method -> method.type == type && method.path == path }
    if (!method) {
      if (scope.containsKey(path)) {
        return scope.get(path)
      }
      throw new IllegalArgumentException("Method not found {$type.name $path}")
    }
    return new MethodExecution(method)
  }

  @CompileStatic
  @PackageScope
  TypedEntity buildTypedEntity(final Type type, final Object arg) {
    return new TypedEntity(type, new TypedEntityValueBuilder(type, scope).from(arg))
  }

  @CompileStatic
  @PackageScope
  Object mapProxy(final Map<String, String> map, final String name) {
    return new ConfigurableStringMap(map, name, scope)
  }

  class MethodExecution {

    /** Method to execute. */
    private final ServiceMethod method

    private def headers = new LinkedHashMap<String, String>(),
                pathParams = new LinkedHashMap<String, String>()

    private def body = null,
                params = null

    public MethodExecution(final ServiceMethod method) {
      this.method = method
    }

    def with(final Closure<?> methodSpec) {
      runWithProxy(new RequestConfiguration(), methodSpec)
      return executor.performMethod(service, method, new ServiceMethodRequestValues(body, params, pathParams, headers))
    }

    class RequestConfiguration {
      void path(final Closure<?> spec) {
        runWithProxy(mapProxy(pathParams, "{path parameters for ${method}}"), spec)
      }
      void httpHeaders(final Closure<?> spec) {
        runWithProxy(mapProxy(headers, "{HTTP httpHeaders for ${method}}"), spec)
      }
      void body(final Object value) {
        body = buildTypedEntity(method.body, value)
      }
      void parameters(final Object value) {
        params = buildTypedEntity(method.parameters, value)
      }
    }

  }

}
