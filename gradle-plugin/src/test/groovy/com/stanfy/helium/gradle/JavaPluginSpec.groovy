package com.stanfy.helium.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Spec for JavaPlugin.
 */
class JavaPluginSpec extends Specification {

  /** Gradle project. */
  Project project

  JavaPlugin javaPlugin
  HeliumPlugin heliumPlugin

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
    project.apply plugin: 'java'
    project.apply plugin: 'helium-java'

    javaPlugin = project.plugins.findPlugin(JavaPlugin)
    heliumPlugin = project.plugins.findPlugin(HeliumPlugin)
  }

  def "should add dependencies to compileJava"() {
    given:
    def compileTask = project.tasks['compileJava']

    when:
    project.helium {
      specification("src/spec/1") {
        sourceGen {
          entities {}
          constants {}
          retrofit {}
        }
      }
      specification("src/spec/2") {
        sourceGen {
          constants {}
        }
      }
    }

    heliumPlugin.createTasks(project)
    JavaPlugin.addGeneratedSources(project)

    then:
    compileTask.taskDependencies.getDependencies(compileTask).size() == 4
  }

  def "should configure sourceSet"() {
    when:
    project.helium {
      specification("src/spec/1") {
        sourceGen {
          entities {}
        }
      }
      specification("src/spec/2") {
        sourceGen {
          constants {}
        }
      }
    }

    heliumPlugin.createTasks(project)
    JavaPlugin.addGeneratedSources(project)

    then:
    project.sourceSets.main.java.srcDirs.size() == 3

  }

}
