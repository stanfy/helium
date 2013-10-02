package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.entities.TypedEntity
import com.stanfy.helium.entities.TypedEntityValueBuilder
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.Type
import com.stanfy.helium.utils.ConfigurableStringMap
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

  public ScenarioDelegate(final Service service, final ScenarioExecutor executor) {
    this.service = service
    this.executor = executor
  }

  private MethodExecution prepareMethodExecution(final MethodType type, final String path) {
    ServiceMethod method = service.methods.find { it.type == type && it.path == path }
    if (!method) { throw new IllegalArgumentException("Method not found {$type.name $path}") }
    return new MethodExecution(method)
  }

  @PackageScope static TypedEntity buildTypedEntity(final Type type, final Object arg) {
    return new TypedEntity(type, new TypedEntityValueBuilder(type).from(arg))
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
        runWithProxy(new ConfigurableStringMap(pathParams, "{path parameters for ${method}}"), spec)
      }
      void httpHeaders(final Closure<?> spec) {
        runWithProxy(new ConfigurableStringMap(headers, "{HTTP headers for ${method}}"), spec)
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
