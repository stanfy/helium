package com.stanfy.helium.handler.codegen.java

import com.stanfy.helium.dsl.ProjectDsl
import spock.lang.Specification

/**
 * Tests for PojoGenerator.
 */
class PojoGeneratorSpec extends Specification {

  PojoGenerator generator
  ProjectDsl project
  File output

  def setup() {
    project = new ProjectDsl()
    project.type "A" message { }
    project.type "B" message { }
    project.type "C" message { }

    output = File.createTempDir()
    generator = new PojoGenerator(output, PojoGeneratorOptions.defaultOptions("com.stanfy.helium"))
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
