package com.stanfy.helium.handler.codegen.tests

import com.stanfy.helium.Helium
import com.stanfy.helium.dsl.SpecExample
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Spec for RestApiTestGenerator.
 */
class RestApiPokeTestsGeneratorSpec extends Specification {

  RestApiPokeTestsGenerator generator = new RestApiPokeTestsGenerator()

  private void run() {
    new Helium().defaultTypes() from SpecExample.example processBy generator
  }

  private void runExampleGenerator() {
    generator.srcOutput = File.createTempDir()
    generator.srcOutput.deleteOnExit()
    run()
  }

  private File findFile(def filter) {
    File res = null
    generator.srcOutput.eachFileRecurse {
      if (res) { return }
      if (filter(it)) {
        res = it
      }
    }
    return res
  }

  def "should require output"() {
    when: run()
    then: thrown(IllegalStateException)
  }

  def "should rewrite spec for tests"() {
    when:
    runExampleGenerator()
    File specFile = findFile { it.name == RestApiMethods.TEST_SPEC_NAME }

    then:
    specFile != null
    specFile.text.contains "type 'UserProfile' message"
    !specFile.text.contains("service")
  }

  def "generated spec must be able to be interpreted"() {
    when:
    runExampleGenerator()
    File specFile = findFile { it.name == RestApiMethods.TEST_SPEC_NAME }
    Type userProfile = null
    new Helium().from(specFile).processBy({ Project project ->
      userProfile = project.types.byName('UserProfile')
    } as Handler)

    then:
    userProfile != null
    userProfile instanceof Message
  }
  
  def "should generate JUnit tests"() {
    when:
    runExampleGenerator()
    int testsCount = 0
    File testFile = null
    generator.srcOutput.eachFileRecurse {
      if (it.name.endsWith("Test.java")) {
        testsCount++
        testFile = it
      }
    }
    def testText = testFile?.text

    then:
    testsCount == 1
    testFile.absolutePath.contains("spec/tests/rest/")
    testText.contains "public class TwitterAPITest extends ${RestApiMethods.simpleName}"
    testText.contains "@Test"
    testText.contains "send(request)"
    testText.contains "validate(response"
    testText.contains MethodType.name

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
    testText.contains "public void product_get_shouldFailWithOutParameters"
    testText.contains "public void product_get_example()"
    testText.contains "product/get?id=23288"

    // post post/example
    testText.contains "public void post_example_shouldFailWithOutBody"
    testText.contains "post/123?full=false"
    !testText.contains("public void post_example_example")

    // post account/add
    testText.contains "public void account_add_shouldFailWithOutBody"
    testText.contains "public void account_add_example"
    testText.contains ".setEntity"
    testText.contains '\\"email\\"' // check escaping

    // headers
    testText.contains 'request.addHeader("User-Agent", "Mozilla")'
    testText.contains 'request.addHeader("Super-Header", "A")'
  }

}
