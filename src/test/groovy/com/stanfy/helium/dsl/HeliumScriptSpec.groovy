package com.stanfy.helium.dsl

import com.stanfy.helium.DefaultTypesLoader
import com.stanfy.helium.HeliumSpec
import spock.lang.Specification

/**
 * Tests for HelliumScript.
 */
class HeliumScriptSpec extends Specification {

  def "DSL methods should be first-class ones"() {
    given:
    ProjectDsl dsl = new ProjectDsl()
    HeliumSpec.loadDefaultTypesFor dsl

    def allTypes = DefaultTypesLoader.openScript().text.split(/\n/).inject([]) { def result, String line ->
      String[] parts = line.split(/\s+/)
      if (parts.length < 2) { return result }
      if (parts[0] == "type" && parts[1].startsWith("")) {
        return result + parts[1][1..-2]
      } else {
        return result
      }
    }

    expect:
    dsl.types.all().collect { it.name }.containsAll allTypes
  }

}
