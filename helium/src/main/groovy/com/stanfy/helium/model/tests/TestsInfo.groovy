package com.stanfy.helium.model.tests

import groovy.transform.CompileStatic

/**
 * Tests information.
 */
@CompileStatic
class TestsInfo {

  /** Whether to use examples. */
  Boolean useExamples

  /** Whether to generate tests that invoke requests with bad input. */
  Boolean generateBadInputTests

  /** Set of predefined HTTP httpHeaders. */
  Map<String, String> httpHeaders = new LinkedHashMap<>()

}
