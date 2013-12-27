package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.codegen.java.PojoGenerator
import com.stanfy.helium.handler.codegen.java.PojoGeneratorOptions
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Task for generating POJOs from a specification.
 */
class GenerateJavaPojoTask extends BaseHeliumTask {

  /** Generator options. */
  PojoGeneratorOptions options;

  @TaskAction
  void generate() {
    if (!options) {
      throw new GradleException("Generation options are not defined");
    }

    helium.processBy new PojoGenerator(output, options)
  }

}
