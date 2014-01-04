package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.codegen.java.constants.ConstantsGeneratorOptions
import com.stanfy.helium.handler.codegen.java.constants.JavaConstantsGenerator
import org.gradle.api.tasks.TaskAction

/**
 * Task for generating constants.
 */
class GenerateJavaConstantsTask extends BaseHeliumTask {

  /** Generator options. */
  ConstantsGeneratorOptions options

  @TaskAction
  void generate() {
    helium.processBy(new JavaConstantsGenerator(output, options))
  }

}
