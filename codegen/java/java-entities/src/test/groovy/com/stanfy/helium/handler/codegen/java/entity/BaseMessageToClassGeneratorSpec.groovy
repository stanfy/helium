package com.stanfy.helium.handler.codegen.java.entity

import com.stanfy.helium.handler.Handler
import com.stanfy.helium.internal.dsl.ProjectDsl
import spock.lang.Specification

/**
 * Base class for Java generator tests.
 */
abstract class BaseMessageToClassGeneratorSpec<T extends Handler> extends Specification {

  ProjectDsl project
  File output
  T generator

  String genFilesPrefix = ""

  def setup() {
    project = new ProjectDsl()
    project.type "A" message { }
    project.type "B" message { }
    project.type "C" message { }

    output = File.createTempDir()
  }

  def "should generate files"() {
    when:
    generator.handle(project)

    then:
    new File("$output/com/stanfy/helium/A${genFilesPrefix}.java").exists()
    new File("$output/com/stanfy/helium/B${genFilesPrefix}.java").exists()
    new File("$output/com/stanfy/helium/C${genFilesPrefix}.java").exists()
  }

  def "should skip anonymous types"() {
    given:
    project.types.byName("B").anonymous = true

    when:
    generator.handle(project)

    then:
    new File("$output/com/stanfy/helium/A${genFilesPrefix}.java").exists()
    !new File("$output/com/stanfy/helium/B${genFilesPrefix}.java").exists()
    new File("$output/com/stanfy/helium/C${genFilesPrefix}.java").exists()
  }

}
