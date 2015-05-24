package com.stanfy.helium.internal.dsl

import com.stanfy.helium.dsl.scenario.MethodExecutionResult
import com.stanfy.helium.dsl.scenario.ScenarioExecutor
import com.stanfy.helium.dsl.scenario.ServiceMethodRequestValues
import com.stanfy.helium.entities.ByteArrayEntity
import com.stanfy.helium.entities.TypedEntity
import com.stanfy.helium.entities.TypedEntityValueBuilder
import com.stanfy.helium.model.FormType
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.Type
import com.stanfy.helium.utils.ConfigurableStringMap
import com.stanfy.helium.utils.Names
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import static com.stanfy.helium.utils.DslUtils.runWithProxy

/**
 * DSL for invoking requests.
 */
class ExecutorDsl {
  static {
    MethodType.values().each { MethodType type ->
      ExecutorDsl.metaClass."${type.name}" << { String path ->
        ExecutorDsl sd = delegate as ExecutorDsl
        return sd.prepareMethodExecution(type, path)
      }
    }
  }

  // TODO: support multiple services.
  /** Underlying service. */
  final Service service

  /** Executor. */
  final ScenarioExecutor executor

  /** Intermediate results collection. */
  @PackageScope
  final LinkedList<MethodExecutionResult> intermediateResults = new LinkedList<>()

  public ExecutorDsl(final Service service, final ScenarioExecutor executor) {
    this.service = service
    this.executor = executor
  }

  @CompileStatic
  private Object prepareMethodExecution(final MethodType type, final String p) {
    String path = Names.rootPath(p)
    ServiceMethod method = service.methods.find { ServiceMethod method -> method.type == type && method.path == path }
    if (!method) {
      throw new IllegalArgumentException("Method not found {$type.name $path}")
    }
    return new MethodExecution(method)
  }

  @CompileStatic
  private static TypedEntity buildTypedEntity(final Type type, final Object arg) {
    return new TypedEntity(type, new TypedEntityValueBuilder(type).from(arg))
  }

  @CompileStatic
  private static Object mapProxy(final Map<String, String> map, final String name) {
    return new ConfigurableStringMap(map, name)
  }

  @Override
  Object invokeMethod(final String name, final Object args) {
    try {
      return getMetaClass().invokeMethod(this, name, args)
    } catch (MissingMethodException e) {
      // If user misses `with` like in
      //   def resp = get "/url" {}
      // we try to detect this here and report.
      if (service.methods.any { it.path == e.method }) {
        throw new IllegalArgumentException(
            "Looks like you missed 'with' after service method uri when trying to invoke a request to $e.method",
            e
        )
      }
      // If method name looks like path, report about an incorrect path.
      if (e.message.contains("/")) {
        throw new IllegalArgumentException(
            "Looks like you specified an incorrect method path $e.method and missed 'with'",
            e
        )
      }
      throw e
    }
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
      MethodExecutionResult res = executor.performMethod(service, method,
          new ServiceMethodRequestValues(body, params, pathParams, headers))
      intermediateResults.add(res)
      return res
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

      // For parsing form-url-encoded, generic data (like bytes) and multipart forms.

      def form(final Object value) {
        if (!(value instanceof Closure<?>)) {
          throw new IllegalArgumentException("Can only build forms from closures.")
        }
        if (!(method.body instanceof FormType)) {
          throw new IllegalArgumentException("Method body type is not form.")
        }
        return value as Closure<?>
      }

      def bytes(final Object value) {
        if (!(value instanceof byte[])) {
          throw new IllegalArgumentException("Not supported type ${value.getClass()} for bytes conversion")
        }
        return new ByteArrayEntity(value as byte[])
      }

      def multipart(final Object value) {
        if (!(value instanceof Closure<?>)) {
          throw new IllegalArgumentException("Can only construct multipart objects with closures.")
        }
        return value as Closure<?>
      }

    }

  }

}
