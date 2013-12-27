package com.stanfy.helium.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import javax.lang.model.element.Modifier

/**
 * Tests for HeliumExtension.
 */
class HeliumExtensionTest extends Specification {

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
    project.extensions.add("helium", HeliumExtension)
    project.helium.attach project
  }

  def "pojo should add generation task"() {
    given:
    project.helium {
      pojo {
        output project.file("../lll")
        options {
          packageName = "some.package"
          prettifyNames = true
          fieldModifiers = [Modifier.FINAL] as Set
        }
      }
    }

    expect:
    project.tasks['generatePojo'] != null
    project.tasks['generatePojo']?.output == project.file("../lll")
    project.tasks['generatePojo']?.options?.packageName == "some.package"
    project.tasks['generatePojo']?.options?.prettifyNames
    project.tasks['generatePojo']?.options?.fieldModifiers == [Modifier.FINAL] as Set
  }

}
