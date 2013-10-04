package com.stanfy.helium.handler.codegen.tests

import com.stanfy.helium.Helium
import spock.lang.Specification

/**
 * Spec for ScenarioTestsGenerator.
 */
class ScenarioTestsGeneratorSpec extends Specification {

  ScenarioTestsGenerator generator

  File spec, out

  def setup() {
    spec = File.createTempFile("hel", "test")
    spec.deleteOnExit()
    spec.withWriter("UTF-8") { Writer out ->
      out << '''
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
      '''
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
    new Helium().from(spec).processBy generator
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
    text.contains "Object proxy" // check field
  }

}
