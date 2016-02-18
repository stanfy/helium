package com.stanfy.helium.gradle

import com.stanfy.helium.gradle.tasks.BaseHeliumTask
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
    return generateSpec(name, null)
  }
  static File generateSpec(final String name, final String code) {
    File tmp = File.createTempFile("aaa", "bbb")
    tmp.deleteOnExit()
    File f = new File(tmp.parentFile, name)
    f.deleteOnExit()
    tmp.parentFile.deleteOnExit()
    f.withWriter {
      it << "type 'A' message { }\n"
      if (code) {
        it << code
      }
    }
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

  def "creates generateSwaggerSpec task"() {
    given:
    project.helium {
      specification generateSpec("abc")
    }

    createTasks()

    expect:
    project.tasks.findByName('generateSwaggerSpec') != null
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
    project.tasks.findByName('checkApiBehaviourS1') != null
    project.tasks.findByName('checkApiBehaviourS2') != null
    project.tasks.findByName('generateSwaggerSpecS1') != null
    project.tasks.findByName('generateSwaggerSpecS2') != null
  }

  def "passes variables to tasks"() {
    given:
    project.helium {
      specification generateSpec("s1", 'def aaa = "$a, $baseDir"')
      variables {
        'a' 'b'
      }
    }

    createTasks()

    def task = project.tasks.findByName('genApiTests')

    expect:
    task instanceof BaseHeliumTask
    ((BaseHeliumTask) task).runWithClassLoader()
    ((BaseHeliumTask) task).variables['a'] == 'b'
  }

  private createTasks() {
    (project.plugins.withType(HeliumPlugin).collect() as List)[0].createTasks(project)
  }

}
