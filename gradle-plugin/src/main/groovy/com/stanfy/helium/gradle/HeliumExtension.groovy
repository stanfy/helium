package com.stanfy.helium.gradle

import com.stanfy.helium.Helium
import com.stanfy.helium.gradle.tasks.GenerateApiTestsTask
import com.stanfy.helium.gradle.tasks.GenerateJavaPojoTask
import com.stanfy.helium.handler.codegen.java.PojoGeneratorOptions
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Helium extension.
 */
class HeliumExtension {

  public static final String VERSION = "0.2-SNAPSHOT"

  /** Tasks group. */
  public static final String GROUP = "Helium"

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(HeliumExtension.class)

  private static final String TESTS_OUT_PATH = "helium/api-tests"

  private static final String HELIUM_ARTIFACT_NAME = "helium"
  public static final String HELIUM_DEP = "com.stanfy.helium:$HELIUM_ARTIFACT_NAME"

  /** Helium. */
  private Helium heliumInstance

  /** Specification location. */
  File specification

  /** Ignore test failures. */
  boolean ignoreFailures

  private Project project

  private def genTasks = []
  private GradleBuild runTestsTask

  void attach(final Project project) {
    this.project = project
  }

  private void initHelium() {
    Helium helium = new Helium().defaultTypes() from this.specification
    this.heliumInstance = helium
    genTasks.each {
      it.helium = helium
    }
  }

  void setSpecification(final File file) {
    this.@specification = file

    GenerateApiTestsTask genTestsTask = project.tasks.create("genApiTests", GenerateApiTestsTask)
    genTestsTask.group = GROUP
    genTestsTask.output = new File(project.buildDir, "source/$TESTS_OUT_PATH")
    genTestsTask.input = file
    genTasks += genTestsTask
    LOG.debug "genApiTests task: json=$genTestsTask.input, output=$genTestsTask.output"

    runTestsTask = project.tasks.create('runApiTests', GradleBuild)
    runTestsTask.group = GROUP
    runTestsTask.buildFile = new File(genTestsTask.output, "build.gradle")
    runTestsTask.dir = genTestsTask.output
    runTestsTask.tasks = ['check']
    runTestsTask.dependsOn genTestsTask
    LOG.debug "runApiTests task: dir=$runTestsTask.dir"

    initHelium()
  }

  void pojo(Closure<?> config) {
    PojoDslDelegate configDelegate = new PojoDslDelegate()
    Closure<?> configAction = config.clone() as Closure<?>
    configAction.delegate = configDelegate
    config.resolveStrategy = Closure.DELEGATE_FIRST
    configAction()

    if (!configDelegate.output) {
      configDelegate.output = new File(project.buildDir, "source/gen/rest-api")
    }

    GenerateJavaPojoTask task = project.tasks.create("generatePojo", GenerateJavaPojoTask)
    task.output = configDelegate.output
    task.options = configDelegate.genOptions
    task.input = specification
    task.helium = heliumInstance
  }

}

