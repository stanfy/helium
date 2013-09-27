package com.stanfy.helium.model.tests

/**
 * Service method test information.
 */
class MethodTestInfo extends TestsInfo {

  /** Path parameters example. */
  Map<String, String> pathExample

  MethodTestInfo resolve(final TestsInfo globalInfo) {
    return new MethodTestInfo(
      useExamples: this.useExamples == null ? !!globalInfo.useExamples : this.useExamples,
      pathExample: pathExample
    )
  }

}
