package com.stanfy.helium.gradle

import com.stanfy.helium.gradle.tasks.GenerateJavaConstantsTask
import com.stanfy.helium.gradle.tasks.GenerateJavaEntitiesTask
import com.stanfy.helium.handler.codegen.java.constants.ConstantNameConverter
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import javax.lang.model.element.Modifier

/**
 * Tests for HeliumExtension.
 */
class HeliumExtensionSpec extends Specification {

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
    project.extensions.add("helium", HeliumExtension.class)
    project.helium.attach project
  }

  def "source generation should support entities generator"() {
    given:

    project.helium {
      File outputDir = project.file("../lll")
      sourceGen {
        entities {
          output outputDir
          options {
            packageName = "some.package"
            prettifyNames = true
            fieldModifiers = [Modifier.FINAL] as Set
          }
        }
      }
    }

    def task = project.tasks['generateEntitiesSomePackage']

    expect:
    task != null
    task.output == project.file("../lll")
    task.options.packageName == "some.package"
    task.options.prettifyNames
    task.options.fieldModifiers == [Modifier.FINAL] as Set
  }

  def "source generation should support constants generator"() {
    given:
    project.helium {
      File outputDir = project.file("../ccc")
      sourceGen {
        constants {
          output outputDir
          options {
            packageName = "some.consts"
            nameConverter = { "COLUMN_${it.canonicalName.toUpperCase(Locale.US)}" } as ConstantNameConverter
          }
        }
      }
    }

    def task = project.tasks['generateConstantsSomeConsts']

    expect:
    task != null
    task.output == project.file("../ccc")
    task.options.packageName == "some.consts"
    task.options.nameConverter.class.name.contains("Proxy")
  }

  def "source generation tasks should be accessible by package name"() {
    given:
    project.helium {
      sourceGen {
        entities {
          options {
            packageName = "p1"
          }
        }
        constants {
          options {
            packageName = "p2"
          }
        }
      }
    }

    expect:
    project.helium.sourceGen.entities['p1'] instanceof GenerateJavaEntitiesTask
    project.helium.sourceGen.constants['p2'] instanceof GenerateJavaConstantsTask
  }

}
