package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.codegen.java.constants.ConstantsGeneratorOptions
import com.stanfy.helium.handler.codegen.java.constants.JavaConstantsGenerator

/**
 * Task for generating constants.
 */
class GenerateJavaConstantsTask extends BaseHeliumTask<ConstantsGeneratorOptions> {

  @Override
  protected void doIt() {
    helium.processBy(new JavaConstantsGenerator(output, options))
  }

}
