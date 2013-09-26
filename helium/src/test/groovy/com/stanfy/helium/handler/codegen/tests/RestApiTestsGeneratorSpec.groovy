package com.stanfy.helium.handler.codegen.tests

import com.stanfy.helium.Helium
import com.stanfy.helium.dsl.SpecExample
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
    generator.srcOutput = File.createTempDir()
    generator.srcOutput.deleteOnExit()
    generator.userAgent = "test agent"
    run()
    int testsCount = 0
    File testFile = null
    File specFile = null
    generator.srcOutput.eachFileRecurse {
      if (it.name == RestApiMethods.TEST_SPEC_NAME) {
        specFile = it
      }
      if (it.name.endsWith("Test.java")) {
        testsCount++
        testFile = it
      }
    }
    def testText = testFile?.text

    then:
    specFile != null
    testsCount == 1
    testFile.absolutePath.contains("spec/tests/rest/")
    testText.contains "public class TwitterAPITest extends ${RestApiMethods.simpleName}"
    testText.contains "@Test"
    testText.contains "public void users_show_json_example"
    testText.contains "public void users_show_json_shouldFailWithOutParameters"
    testText.contains "send(request)"
    testText.contains "validate(response"
    testText.contains 'setUserAgent("test agent")'

  }

}
