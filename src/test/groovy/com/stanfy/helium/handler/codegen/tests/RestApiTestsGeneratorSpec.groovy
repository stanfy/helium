package com.stanfy.helium.handler.codegen.tests

import com.stanfy.helium.Helium
import com.stanfy.helium.dsl.SpecExample
import com.stanfy.helium.handler.codegen.tests.RestApiTestsGenerator
import spock.lang.Specification

/**
 * Spec for RestApiTestGenerator.
 */
class RestApiTestsGeneratorSpec extends Specification {

  RestApiTestsGenerator generator = new RestApiTestsGenerator()

  private void run() {
    new Helium().defaultTypes() from SpecExample.example processBy generator
  }

  def "should require output"() {
    when: run()
    then: thrown(IllegalStateException)
  }

  def "should generate JUnit tests"() {
    when:
    generator.output = File.createTempDir()
    generator.output.deleteOnExit()
    run()
    int testsCount = 0
    File testFile = null
    generator.output.eachFileRecurse {
      if (it.name.endsWith("Test.java")) {
        testsCount++
        testFile = it
      }
    }
    def testText = testFile?.text

    then:
    testsCount == 1
    testText.contains "public class TwitterAPITest"
  }

}
