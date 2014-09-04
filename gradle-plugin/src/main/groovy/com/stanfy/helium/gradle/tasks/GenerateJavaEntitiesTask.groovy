package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.codegen.java.entity.EntitiesGenerator
import com.stanfy.helium.handler.codegen.java.entity.EntitiesGeneratorOptions
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Task for generating POJOs from a specification.
 */
class GenerateJavaEntitiesTask extends BaseHeliumTask<EntitiesGeneratorOptions> {

  @Override
  protected void doIt() {
    helium.processBy new EntitiesGenerator(output, options)
  }

}
