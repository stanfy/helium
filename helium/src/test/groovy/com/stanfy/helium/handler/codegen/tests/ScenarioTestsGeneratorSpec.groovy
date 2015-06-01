package com.stanfy.helium.handler.codegen.tests

import com.stanfy.helium.Helium
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.dsl.scenario.ScenarioInvoker
import spock.lang.Specification

/**
 * Spec for ScenarioTestsGenerator.
 */
class ScenarioTestsGeneratorSpec extends Specification {

  ScenarioTestsGenerator generator

  File spec, out

  File includedSpec
  String baseDir

  def setup() {
    spec = File.createTempFile("hel", "test-spec")
    spec.deleteOnExit()

    def includedSpecUri = getClass().getResource("/include-nested.spec").toURI()
    includedSpec = new File(includedSpecUri)
    baseDir = includedSpec.parentFile.toURI().toString()

    spec.withWriter("UTF-8") { Writer out ->
      out << """
        include "\${baseDir}/${includedSpec.name}"
        service {
          name "Main"
          tests {
            scenario "all actions", before: {}, after: {} spec {}
            scenario "main only" spec {}
            scenario "with after", after: {} spec {}
            scenario "with before", before: {} spec {}
          }
        }
        service {
          name "Help"
          tests {
            scenario "test" spec {}
          }
        }
        service {
          name "Test 3"
        }
      """
    }
    out = File.createTempDir()
    out.deleteOnExit()
    generator = new ScenarioTestsGenerator(spec, out)
  }

  private def findFiles(def filter) {
    def res = []
    out.eachFileRecurse {
      if (filter(it)) {
        res += it
      }
    }
    return res
  }

  private void run() {
    new Helium().set("baseDir", baseDir).set("v1", "value").from(spec).processBy generator
  }

  def "generates file per service"() {
    given:
    run()
    def files = findFiles { it.name.endsWith 'Test.java' }

    expect:
    files.size() == 2
  }

  def "generates method per scenario"() {
    when:
    run()
    def file = (findFiles { it.name == 'MainScenariosTest.java' })[0]
    def text = file?.text

    then:
    file != null
    text != null
    ["all_actions", "main_only", "with after", "with before"].every { text.contains(it) }
    text.contains "@Test"

    text.contains "Service service" // check field
    text.contains "ScenarioDelegate proxy" // check field
  }

  def "uses ScenarioInvoker"() {
    when:
    run()
    def text = (findFiles { it.name == 'MainScenariosTest.java' })[0]?.text

    then:
    text != null
    text.contains "${ScenarioInvoker.class.name}.invokeScenario(proxy"
  }

  def "good message for missing service name"() {
    when:
    ProjectDsl p = new ProjectDsl()
    p.service { }
    generator.handle(p)

    then:
    def e = thrown(IllegalStateException)
    e.message.contains "service name"
  }

  def "set variables"() {
    when:
    run()
    def text = (findFiles { it.name == 'MainScenariosTest.java' })[0]?.text
    then:
    text.contains "void prepareVariables(final Helium helium) {"
    !text.contains("helium.set(\"baseDir\"")
    text.contains "helium.set(\"v1\", \"value\");"
  }

}
