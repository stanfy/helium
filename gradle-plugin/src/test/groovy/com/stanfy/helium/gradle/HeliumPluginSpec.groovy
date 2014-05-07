package com.stanfy.helium.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Spec for HeliumPlugin.
 */
class HeliumPluginSpec extends Specification {

  /** Gradle project. */
  Project project

  static File generateSpec(final String name) {
    File tmp = File.createTempFile("aaa", "bbb")
    tmp.deleteOnExit()
    File f = new File(tmp.parentFile, name)
    f.deleteOnExit()
    tmp.parentFile.deleteOnExit()
    f << "type 'string'"
    return f
  }

  def setup() {
    project = ProjectBuilder.builder().build()
    project.apply plugin: 'helium'
  }

  def "creates genApiTests task"() {
    given:
    project.helium {
      specification generateSpec("abc")
    }

    createTasks()

    expect:
    project.tasks.findByName('genApiTests') != null
    project.tasks.findByName('runApiTests') != null
  }

  def "creates tasks for every specification"() {
    given:
    project.helium {
      specification generateSpec("s1")
      specification generateSpec("s2")
    }

    createTasks()

    expect:
    project.tasks.findByName('genApiTestsS1') != null
    project.tasks.findByName('runApiTestsS1') != null
    project.tasks.findByName('genApiTestsS2') != null
    project.tasks.findByName('runApiTestsS2') != null
  }

  private createTasks() {
    (project.plugins.withType(HeliumPlugin).collect() as List)[0].createTasks(project)
  }

}
