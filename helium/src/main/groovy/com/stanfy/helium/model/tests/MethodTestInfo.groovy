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
    def headersMap = new LinkedHashMap<String, Object>()
    headersMap.putAll(globalInfo.httpHeaders)
    headersMap.putAll(httpHeaders)

    return new MethodTestInfo(
      useExamples: this.useExamples == null ? !!globalInfo.useExamples : this.useExamples,
      pathExample: pathExample,
      httpHeaders: headersMap
    )
  }

}
