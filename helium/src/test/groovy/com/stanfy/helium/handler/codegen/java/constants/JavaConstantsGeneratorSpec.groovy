package com.stanfy.helium.handler.codegen.java.constants

import com.stanfy.helium.handler.codegen.java.BaseGeneratorSpec

/**
 * Tests for JavaConstantsGenerator.
 */
class JavaConstantsGeneratorSpec extends BaseGeneratorSpec<JavaConstantsGenerator> {

  /** Options. */
  ConstantsGeneratorOptions options

  def setup() {
    options = ConstantsGeneratorOptions.defaultOptions("com.stanfy.helium")
    generator = new JavaConstantsGenerator(output, options)
    genFilesPrefix = "Constants"
  }

}
