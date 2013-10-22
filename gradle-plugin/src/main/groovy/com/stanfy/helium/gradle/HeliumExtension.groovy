package com.stanfy.helium.gradle

import com.stanfy.helium.Helium
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

  private GenerateApiTestsTask genTask
  private GradleBuild runTask

  void attach(final Project project) {
    this.project = project
  }

  void setSpecification(final File file) {
    this.@specification = file

    heliumInstance = new Helium().defaultTypes() from file

    genTask = project.tasks.create("genApiTests", GenerateApiTestsTask)
    genTask.group = GROUP
    genTask.helium = heliumInstance
    genTask.output = new File(project.buildDir, "source/$TESTS_OUT_PATH")
    genTask.input = file
    LOG.debug "genApiTests task: json=$genTask.input, output=$genTask.output"

    runTask = project.tasks.create('runApiTests', GradleBuild)
    runTask.group = GROUP
    runTask.buildFile = new File(genTask.output, "build.gradle")
    runTask.dir = genTask.output
    runTask.tasks = ['check']
    runTask.dependsOn genTask
    LOG.debug "runApiTests task: dir=$runTask.dir"
  }

}
