package com.stanfy.helium.gradle

import com.stanfy.helium.Helium
import com.stanfy.helium.handler.codegen.tests.RestApiTestsGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Generates API tests using the specification.
 */
class GenerateApiTestsTask extends DefaultTask {

  /** Helium instance. */
  Helium helium

  /** Input specification file. */
  @InputFile
  File input

  /** Output directory. */
  @OutputDirectory
  File output

  @TaskAction
  void generate() {
    helium.processBy new RestApiTestsGenerator(output: output)
  }

}
