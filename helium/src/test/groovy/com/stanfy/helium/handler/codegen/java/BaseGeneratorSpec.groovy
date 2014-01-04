package com.stanfy.helium.handler.codegen.java

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.Handler
import spock.lang.Specification

/**
 * Base class for Java generator tests.
 */
abstract class BaseGeneratorSpec<T extends Handler> extends Specification {

  ProjectDsl project
  File output
  T generator

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
    new File("$output/com/stanfy/helium/A.java").exists()
    new File("$output/com/stanfy/helium/B.java").exists()
    new File("$output/com/stanfy/helium/C.java").exists()
  }

}
