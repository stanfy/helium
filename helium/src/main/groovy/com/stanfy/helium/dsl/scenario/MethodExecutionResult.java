package com.stanfy.helium.dsl.scenario;

import java.util.List;

/**
 * Service method execution result.
 */
public interface MethodExecutionResult {

  List<AssertionError> getInteractionErrors();

}
