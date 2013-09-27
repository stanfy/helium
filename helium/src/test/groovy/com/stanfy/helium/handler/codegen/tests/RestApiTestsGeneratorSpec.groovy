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
    testText.contains "send(request)"
    testText.contains "validate(response"

    // get users/show.json
    testText.contains "public void users_show_json_shouldFailWithOutParameters"
    // get test/@param.json
    testText.contains "public void test_param_json_example()"
    testText.contains "test/value.json"
    // get simple/request
    testText.contains "public void simple_request_example()"
    // get required/@example
    testText.contains "public void required_example_example()"
    testText.contains "required/HOP?param1=2"
    // get product/get
    testText.contains "public void product_get_example()"
    testText.contains "product/get?id=23288"

    // headers
    testText.contains 'request.addHeader("User-Agent", "Mozilla")'
    testText.contains 'request.addHeader("Super-Header", "A")'
  }

}
