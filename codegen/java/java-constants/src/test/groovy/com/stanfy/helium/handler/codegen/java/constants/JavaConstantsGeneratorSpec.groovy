package com.stanfy.helium.handler.codegen.java.constants

import com.stanfy.helium.handler.codegen.java.BaseMessageToClassGeneratorSpec

/**
 * Tests for JavaConstantsGenerator.
 */
class JavaConstantsGeneratorSpec extends BaseMessageToClassGeneratorSpec<JavaConstantsGenerator> {

  /** Options. */
  ConstantsGeneratorOptions options

  def setup() {
    options = ConstantsGeneratorOptions.defaultOptions("com.stanfy.helium")
    generator = new JavaConstantsGenerator(output, options)
    genFilesPrefix = "Constants"
  }

}
