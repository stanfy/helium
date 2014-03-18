package com.stanfy.helium.dsl.scenario;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service method execution result.
 */
public interface MethodExecutionResult {

  List<AssertionError> getInteractionErrors();

  Object getBody() throws IOException;

  Map<String, String> getHttpHeaders();

  void mustSucceed();

  void mustBeClientError();

  boolean isSuccessful();

}
