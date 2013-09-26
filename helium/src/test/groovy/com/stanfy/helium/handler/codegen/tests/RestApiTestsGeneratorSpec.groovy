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
    File specFile = null
    generator.output.eachFileRecurse {
      if (it.name == RestApiMethods.TEST_SPEC_NAME) {
        specFile = it
      }
      if (it.name.endsWith("Test.java")) {
        testsCount++
        testFile = it
      }
    }
    def testText = testFile?.text
    println testText

    then:
    specFile != null
    testsCount == 1
    testText.contains "public class TwitterAPITest extends ${RestApiMethods.simpleName}"
    testText.contains "@Test"
    testText.contains "public void users_show_json"
    testText.contains "send(request)"
    testText.contains "validate(response"

  }

}
