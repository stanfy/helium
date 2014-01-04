package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.codegen.java.entity.EntitiesGenerator
import com.stanfy.helium.handler.codegen.java.entity.EntitiesGeneratorOptions
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Task for generating POJOs from a specification.
 */
class GenerateJavaEntitiesTask extends BaseHeliumTask {

  /** Generator options. */
  @Input
  EntitiesGeneratorOptions options;

  @TaskAction
  void generate() {
    helium.processBy new EntitiesGenerator(output, options)
  }

}
