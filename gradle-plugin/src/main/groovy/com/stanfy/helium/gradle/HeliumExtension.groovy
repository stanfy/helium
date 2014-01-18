package com.stanfy.helium.gradle

import com.stanfy.helium.Helium
import com.stanfy.helium.gradle.SourceGenDslDelegate.ConstantsDslDelegate
import com.stanfy.helium.gradle.SourceGenDslDelegate.EntitiesDslDelegate
import com.stanfy.helium.gradle.tasks.BaseHeliumTask
import com.stanfy.helium.gradle.tasks.GenerateApiTestsTask
import com.stanfy.helium.gradle.tasks.GenerateJavaConstantsTask
import com.stanfy.helium.gradle.tasks.GenerateJavaEntitiesTask
import com.stanfy.helium.handler.codegen.java.JavaGeneratorOptions
import com.stanfy.helium.utils.DslUtils
import com.stanfy.helium.utils.Names
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Helium extension.
 */
class HeliumExtension {

  public static final String VERSION = "0.3.4-SNAPSHOT"

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

  /** Source generation tasks. */
  private final SourceGenerationTasks sourceGenTasks = new SourceGenerationTasks()

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

  void sourceGen(Closure<?> config) {
    SourceGenDslDelegate delegate = new SourceGenDslDelegate(config.owner)
    DslUtils.runWithProxy(delegate, config)
    processEntities(delegate.entities)
    processConstants(delegate.constants)
  }

  SourceGenerationTasks getSourceGen() {
    return sourceGenTasks
  }

  private void processEntities(EntitiesDslDelegate entities) {
    if (!entities) {
      return
    }

    if (!entities.output) {
      entities.output = new File(project.buildDir, "source/gen/rest-api")
    }
    GenerateJavaEntitiesTask task = project.tasks.create(
        taskName("generateEntities", entities.genOptions),
        GenerateJavaEntitiesTask
    )
    genTasks += task
    configureGenTask(task, entities.output)
    task.options = entities.genOptions
    sourceGen.entities[entities.genOptions.packageName] = task
  }

  private void configureGenTask(BaseHeliumTask task, File output) {
    task.output = output
    task.input = specification
    task.helium = heliumInstance
  }

  private void processConstants(ConstantsDslDelegate constants) {
    if (!constants) {
      return
    }

    if (!constants.output) {
      constants.output = new File(project.buildDir, "source/gen/constants")
    }
    GenerateJavaConstantsTask task = project.tasks.create(
        taskName("generateConstants", constants.genOptions),
        GenerateJavaConstantsTask
    )
    genTasks += task
    configureGenTask(task, constants.output)
    task.options = constants.genOptions
    sourceGen.constants[constants.genOptions.packageName] = task
  }

  private String taskName(final String prefix, final JavaGeneratorOptions options) {
    String pkgSuffix = Names.prettifiedName(Names.canonicalName(options.packageName))
    return "$prefix${pkgSuffix.capitalize()}"
  }

  public static class SourceGenerationTasks {
    Map<String, BaseHeliumTask> entities = [:]
    Map<String, BaseHeliumTask> constants = [:]
  }

}
