package com.stanfy.helium.model.tests

import groovy.transform.CompileStatic

/**
 * Service method test information.
 */
@CompileStatic
class MethodTestInfo extends TestsInfo {

  /** Path parameters example. */
  Map<String, String> pathExample

  MethodTestInfo resolve(final TestsInfo globalInfo) {
    def headersMap = new LinkedHashMap<String, String>()
    headersMap.putAll(globalInfo.httpHeaders)
    headersMap.putAll(httpHeaders)

    boolean globalUseExamples = globalInfo.useExamples == null ? false : globalInfo.useExamples
    boolean globalBadInput = globalInfo.generateBadInputTests == null ? false : globalInfo.generateBadInputTests
    return new MethodTestInfo(
      useExamples: this.useExamples == null ? globalUseExamples : this.useExamples,
      generateBadInputTests: this.generateBadInputTests == null ? globalBadInput : this.generateBadInputTests,
      pathExample: pathExample,
      httpHeaders: headersMap
    )
  }

}
