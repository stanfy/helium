package com.stanfy.helium.gradle

import com.stanfy.helium.Helium
import com.stanfy.helium.handler.codegen.tests.RestApiPokeTestsGenerator
import com.stanfy.helium.handler.codegen.tests.ScenarioTestsGenerator
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
    File sourcesDir = new File(output, "src/test/java")
    sourcesDir.mkdirs()
    File resDir = new File(output, "src/test/resources")
    resDir.mkdirs()
    helium.processBy new RestApiPokeTestsGenerator(sourcesDir, resDir)
    helium.processBy new ScenarioTestsGenerator(input, sourcesDir, resDir)

    File buildFile = new File(output, "build.gradle")
    buildFile.withWriter('UTF-8') {
      it << """
apply plugin: 'java'

repositories {
  mavenCentral()
  mavenLocal()
  maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
}

test {
  ignoreFailures = ${project.helium.ignoreFailures}
}

dependencies {
  testCompile '${HeliumExtension.HELIUM_DEP}:${HeliumExtension.VERSION}'
  testCompile 'junit:junit:4.11'
}
"""
    }



  }

}
