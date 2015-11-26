package com.stanfy.helium.model.behaviour;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service method execution result.
 */
public interface MethodExecutionResult {

  Object getBody() throws IOException;

  Map<String, String> getHttpHeaders();

  int getStatusCode();

  void mustSucceed();

  void mustBeClientError();

  boolean isSuccessful();

  List<AssertionError> getInteractionErrors();

}
