package com.stanfy.helium.handler.codegen.tests;

import com.stanfy.helium.model.HttpHeader;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.ServiceMethod;
import com.stanfy.helium.model.tests.MethodTestInfo;

/**
 * Some tools used for tests generation.
 */
final class Utils {

  private Utils() { }

  static MethodTestInfo preparePokeTestInfo(final ServiceMethod method, final Service service) {
    MethodTestInfo testInfo = method.getTestInfo().resolve(service.getTestInfo());
    for (HttpHeader h : method.getHttpHeaders()) {
      if (h.isConstant()) {
        testInfo.getHttpHeaders().put(h.getName(), h.getValue());
      } else if (h.getExamples() != null && !h.getExamples().isEmpty()) {
        testInfo.getHttpHeaders().put(h.getName(), h.getExamples().get(0));
      }
    }
    return testInfo;
  }

}
