package com.stanfy.helium.model.tests

import groovy.transform.CompileStatic

/**
 * Tests information.
 */
@CompileStatic
class TestsInfo {

  /** Whether to use examples. */
  Boolean useExamples

  /** Set of predefined HTTP headers. */
  Map<String, Object> httpHeaders = new LinkedHashMap<>()

}
