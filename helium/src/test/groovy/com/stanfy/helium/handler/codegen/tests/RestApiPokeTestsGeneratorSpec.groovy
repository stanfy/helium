package com.stanfy.helium.handler.codegen.tests

import com.stanfy.helium.Helium
import com.stanfy.helium.dsl.ProjectDsl
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

  RestApiPokeTestsGenerator generator

  private void runExampleGenerator() {
    File output = File.createTempDir()
    output.deleteOnExit()
    generator = new RestApiPokeTestsGenerator(output)
    new Helium().defaultTypes() from SpecExample.example processBy generator
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
    when: new RestApiPokeTestsGenerator(null)
    then: thrown(IllegalArgumentException)
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

  def "does not generate file for with zero methods"() {
    when:
    File output = File.createTempDir()
    output.deleteOnExit()
    generator = new RestApiPokeTestsGenerator(output)
    new Helium().defaultTypes() from { service { name "fake" } } processBy generator

    int testsCount = 0
    generator.srcOutput.eachFileRecurse {
      if (it.name.endsWith("Test.java")) {
        testsCount++
      }
    }

    then:
    testsCount == 0
  }

  private File runAndGetMainOutput() {
    runExampleGenerator()
    int testsCount = 0
    File testFile = null
    generator.srcOutput.eachFileRecurse {
      if (it.name.endsWith("Test.java")) {
        testsCount++
        testFile = it
      }
    }
    assert testsCount == 1
    return testFile
  }

  def "should generate JUnit tests"() {
    when:
    def testFile = runAndGetMainOutput()
    def testText = testFile?.text

    then:
    testFile.absolutePath.contains("spec${File.separatorChar}tests${File.separatorChar}rest")
    testText.contains "public class Twitter_APIPokeTest extends ${RestApiMethods.simpleName}"
    testText.contains "@Test"
    testText.contains "getClient().newCall(request).execute()"
    testText.contains "validate(response,"
    testText.contains MethodType.name

    // get users/show.json
    testText.contains "public void get_users_show_json_shouldFailWithOutParameters"
    // get test/@param.json
    testText.contains "public void get_test_param_json_example()"
    testText.contains "test/value.json"
    // get simple/request
    testText.contains "public void get_simple_request_example()"
    // get required/@example
    testText.contains "public void get_required_example_example()"
    testText.contains "required/HOP?param1=2"
    // get product/get
    testText.contains "public void get_product_get_shouldFailWithOutParameters"
    testText.contains "public void get_product_get_example()"
    testText.contains "product/get?id=23288"

    // post post/example
    testText.contains "public void post_post_example_shouldFailWithOutBody"
    testText.contains "post/123?full=false"
    !testText.contains("public void post_post_example_example")

    // post account/add
    testText.contains "public void post_account_add_shouldFailWithOutBody"
    testText.contains "public void post_account_add_example"
    testText.contains "RequestBody.create("
    testText.contains '\\"email\\"' // check escaping

    // should not contain test for path parameters without examples
    !testText.contains("testNoExamples")

    // httpHeaders
    testText.contains 'rb.header("User-Agent", "Mozilla")'
    testText.contains 'rb.header("Super-Header", "A")'
    testText.contains 'Request request = rb.build();'

    // no bad inputs
    !testText.contains('public void get_test_no_bad_input_shouldFail')

    // respects unresolved path param
    !testText.contains("with/unresolved/path/@parameter")

    // nothing if headers are not resolved
    !testText.contains("with/header/no/poke/test")
    // use constant header
    testText.contains("with/constant/header")
    testText.contains 'rb.header("ConstantHeader", "v1")'
    // resolve header example
    testText.contains("with/header/example")
    testText.contains 'rb.header("RequiredHeader", "e1")'

  }

  def "good message for missing service name"() {
    when:
    ProjectDsl p = new ProjectDsl()
    p.service { }
    File output = File.createTempDir()
    output.deleteOnExit()
    generator = new RestApiPokeTestsGenerator(output)
    generator.handle(p)

    then:
    def e = thrown(IllegalStateException)
    e.message.contains "service name"
  }

  def "import okhttp"() {
    given:
    def testText = runAndGetMainOutput()?.text

    expect:
    testText.contains "com.squareup.okhttp.OkHttpClient"
    testText.contains "com.squareup.okhttp.Request"
    testText.contains "com.squareup.okhttp.Response"
    testText.contains "com.squareup.okhttp.RequestBody"
  }

}
