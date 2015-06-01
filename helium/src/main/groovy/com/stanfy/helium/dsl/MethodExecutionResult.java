package com.stanfy.helium.dsl;

import java.io.IOException;
import java.util.Map;

/**
 * Service method execution result.
 */
public interface MethodExecutionResult {

  /**
   * @see com.stanfy.helium.entities.TypedEntity#value
   */
  Object getBody() throws IOException;

  Map<String, String> getHttpHeaders();

  int getStatusCode();

  void mustSucceed();

  void mustBeClientError();

  boolean isSuccessful();

}
