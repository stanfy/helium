package com.stanfy.helium.gradle

import com.stanfy.helium.gradle.tasks.GenerateJavaConstantsTask
import com.stanfy.helium.gradle.tasks.GenerateJavaEntitiesTask
import com.stanfy.helium.handler.codegen.java.constants.ConstantNameConverter
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import javax.lang.model.element.Modifier

import static com.stanfy.helium.gradle.HeliumPluginSpec.generateSpec

/**
 * Tests for HeliumExtension.
 */
class HeliumPuginSourceGenSpec extends Specification {

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
    project.apply plugin: 'helium'
    project.helium {
      specification generateSpec("s1")
    }
  }

  private void createTasks() {
    (project.plugins.withType(HeliumPlugin).collect() as List)[0].createTasks(project)
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

    createTasks()

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

    createTasks()

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

    createTasks()

    expect:
    project.helium.sourceGen.entities['p1'] instanceof GenerateJavaEntitiesTask
    project.helium.sourceGen.constants['p2'] instanceof GenerateJavaConstantsTask
  }

  def "source generation tasks are created per specification"() {
    given:
    project.helium {
      // 2nd spec
      specification(generateSpec("s2.api")) {
        sourceGen {
          entities {
            options {
              packageName = "p2"
            }
          }
        }
      }
      sourceGen {
        entities {
          options {
            packageName = "p1"
          }
        }
      }
    }
    createTasks()

    expect:
    project.helium.specifications.size() == 2
    project.helium.sourceGen('s1').entities['p1'] instanceof GenerateJavaEntitiesTask
    project.helium.sourceGen('s2').entities['p2'] instanceof GenerateJavaEntitiesTask
    !project.helium.sourceGen('s2').entities['p1']
    !project.helium.sourceGen('s1').entities['p2']
  }

  def "default sourceGen property is not available if there are multiple specifications"() {
    when:
    project.helium {
      specification generateSpec("s2.api")
      sourceGen {
        entities {
          options {
            packageName = "p1"
          }
        }
      }
    }
    def task = project.helium.sourceGen.entities['p1']

    then:
    def e = thrown(GradleException)
    e.message.contains("multiple specifications")
  }

}
