package com.stanfy.helium

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.model.Project
import spock.lang.Specification

/**
 * Spec for Helium.
 */
class HeliumSpec extends Specification {

  /** Instance under the test. */
  Helium helium = new Helium()
  /** Stats handler. */
  StatsHandler handler = new StatsHandler()

  public static loadDefaultTypesFor(final ProjectDsl project) {
    DefaultTypesLoader.loadFor(project)
  }

  private static Closure<?> simpleClosure() {
    new GroovyShell().evaluate("return {${simpleScript()}}") as Closure<?>
  }

  private static def simpleScript() {
    return '''
      type "2"
      service { name "s1" }
      type "m1" message { }
      note "end"
      note "інша мова"
    '''
  }

  def "can handle dsl from closure"() {
    when:
    helium.from simpleClosure() processBy handler

    then:
    handler.notesCount == 2
    handler.messagesCount == 1
    handler.structureUnitsCount == 5
    handler.servicesCount == 1
    handler.typesCount == 2
  }

  def "can load default types"() {
    when:
    helium.from simpleClosure() defaultTypes() processBy handler

    then:
    handler.notesCount == 2
    handler.messagesCount == 1
    handler.structureUnitsCount > 5
    handler.servicesCount == 1
    handler.typesCount > 2
  }

  def "can handle dsl from reader"() {
    when:
    helium.from new StringReader(simpleScript()) processBy handler

    then:
    handler.notesCount == 2
    handler.messagesCount == 1
    handler.structureUnitsCount == 5
    handler.servicesCount == 1
    handler.typesCount == 2
  }

  def "can handle dsl from string"() {
    when:
    helium.from simpleScript() processBy handler

    then:
    handler.notesCount == 2
    handler.messagesCount == 1
    handler.structureUnitsCount == 5
    handler.servicesCount == 1
    handler.typesCount == 2
  }

  def "can handle dsl from file"() {
    when:
    File f = File.createTempFile("abc", "abc")
    f.deleteOnExit()
    f.withOutputStream {
      it << simpleScript().getBytes("UTF-8")
    }
    helium.from f processBy handler

    then:
    handler.notesCount == 2
    handler.messagesCount == 1
    handler.structureUnitsCount == 5
    handler.servicesCount == 1
    handler.typesCount == 2
  }

  def "respects file encoding"() {
    when:
    File f = File.createTempFile("abc", "abc")
    f.deleteOnExit()
    f.withOutputStream {
      it << simpleScript().getBytes("windows-1251")
    }
    helium.encoding "windows-1251" from f processBy handler

    then:
    handler.notesCount == 2
    handler.messagesCount == 1
    handler.structureUnitsCount == 5
    handler.servicesCount == 1
    handler.typesCount == 2
  }

  def "sets variables"() {
    when:
    helium.set "count", 3 from 'count.times() { note "$it" }' processBy handler

    then:
    handler.notesCount == 3
  }

}

/** Handler for tests. */
class StatsHandler implements Handler {

  int notesCount, messagesCount, structureUnitsCount, servicesCount, typesCount

  @Override
  void handle(Project project) {
    notesCount = project.notes.size()
    messagesCount = project.messages.size()
    structureUnitsCount = project.structure.size()
    servicesCount = project.services.size()
    typesCount = project.types.all().collect {it}.size()
  }

}
