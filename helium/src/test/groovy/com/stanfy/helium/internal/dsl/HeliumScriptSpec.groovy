package com.stanfy.helium.internal.dsl

import com.stanfy.helium.internal.handler.ScriptExtender
import spock.lang.Specification

/**
 * Tests for HelliumScript.
 */
class HeliumScriptSpec extends Specification {

  def "DSL methods should be first-class ones"() {
    given:
    ProjectDsl dsl = new ProjectDsl()
    def text = HeliumScriptSpec.classLoader.getResourceAsStream("com/stanfy/helium/test/test.spec").text

    new ScriptExtender(new StringReader(text), 'Test.spec', 'path').handle(dsl)

    def allTypes = text.split(/\n/).inject([]) { def result, String line ->
      String[] parts = line.split(/\s+/)
      if (parts.length < 2) { return result }
      if (parts[0] == "type" && parts[1].startsWith("")) {
        return result + parts[1][1..-2]
      } else {
        return result
      }
    }

    expect:
    dsl.notes[0].value == '123'
    dsl.types.all().collect { it.name }.containsAll allTypes
  }

}
