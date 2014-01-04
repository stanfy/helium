package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.codegen.java.entity.EntitiesGenerator
import com.stanfy.helium.handler.codegen.java.entity.EntitiesGeneratorOptions
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Task for generating POJOs from a specification.
 */
class GenerateJavaPojoTask extends BaseHeliumTask {

  /** Generator options. */
  EntitiesGeneratorOptions options;

  @TaskAction
  void generate() {
    if (!options) {
      throw new GradleException("Generation options are not defined");
    }

    helium.processBy new EntitiesGenerator(output, options)
  }

}
