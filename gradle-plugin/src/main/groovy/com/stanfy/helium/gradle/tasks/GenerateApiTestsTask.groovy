package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.gradle.HeliumExtension
import com.stanfy.helium.handler.codegen.tests.RestApiPokeTestsGenerator
import com.stanfy.helium.handler.codegen.tests.ScenarioTestsGenerator
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

/**
 * Generates API tests using the specification.
 */
class GenerateApiTestsTask extends BaseHeliumTask {

  @Override
  protected void doIt() {
    File sourcesDir = new File(output, "src/test/java")
    sourcesDir.mkdirs()
    File resDir = new File(output, "src/test/resources")
    resDir.mkdirs()
    helium.processBy new RestApiPokeTestsGenerator(sourcesDir, resDir)
    helium.processBy new ScenarioTestsGenerator(input, sourcesDir, resDir)

    FileCollection classpath = project.configurations.helium
    def classpathString = ""
    if (classpath) {
      String all = classpath.inject("[]", { String acc, File f -> acc + ", '" + f.absolutePath + "'" })
      classpathString = "classpath += files($all)"
    }

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
  ${classpathString}
  scanForTestClasses = false
  include '**/*Test.class'
}

dependencies {
  testCompile '${HeliumExtension.HELIUM_DEP}:${HeliumExtension.VERSION}'
  testCompile 'junit:junit:4.11'
}
"""
    }



  }

}
