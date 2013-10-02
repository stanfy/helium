package com.stanfy.helium.dsl.scenario

import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod

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

  class MethodExecution {

    /** Method to execute. */
    private final ServiceMethod method

    public MethodExecution(final ServiceMethod method) {
      this.method = method
    }

    def with(final Closure<?> methodSpec) {
      // TODO: parametrize with methodSpec
      return executor.performMethod(service, method)
    }
  }

}
