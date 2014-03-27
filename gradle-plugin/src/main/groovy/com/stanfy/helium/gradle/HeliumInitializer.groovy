package com.stanfy.helium.gradle

import com.stanfy.helium.gradle.tasks.BaseHeliumTask
import com.stanfy.helium.gradle.tasks.GenerateApiTestsTask
import com.stanfy.helium.gradle.tasks.GenerateJavaConstantsTask
import com.stanfy.helium.gradle.tasks.GenerateJavaEntitiesTask
import com.stanfy.helium.handler.codegen.java.JavaGeneratorOptions
import com.stanfy.helium.utils.Names
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.stanfy.helium.gradle.HeliumExtension.GROUP

@PackageScope
final class HeliumInitializer implements TasksCreator {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(HeliumInitializer.class)

  private static final String TESTS_OUT_PATH = "helium/api-tests"

  /** Config object. */
  private final HeliumExtension config
  /** User configuration. */
  private final Config userConfig;

  private File specification

  public HeliumInitializer(final HeliumExtension config, final Config userConfig) {
    this.config = config
    this.userConfig = userConfig
  }

  @Override
  void createTasks(ClassLoader classLoader) {
    Project project = userConfig.project
    specification = config.specification

    if (specification) {
      // tests generation task
      GenerateApiTestsTask genTestsTask = project.tasks.create("genApiTests", GenerateApiTestsTask)
      genTestsTask.group = GROUP
      configureHeliumTask(genTestsTask, new File(project.buildDir, "source/$TESTS_OUT_PATH"), classLoader)
      LOG.debug "genApiTests task: json=$genTestsTask.input, output=$genTestsTask.output"

      // tests run task
      GradleBuild runTestsTask = project.tasks.create('runApiTests', GradleBuild)
      runTestsTask.group = GROUP
      runTestsTask.buildFile = new File(genTestsTask.output, "build.gradle")
      runTestsTask.dir = genTestsTask.output
      runTestsTask.tasks = ['check']
      runTestsTask.dependsOn genTestsTask
      LOG.debug "runApiTests task: dir=$runTestsTask.dir"
    }

    // source generation
    if (userConfig.sourceGeneration) {
      processEntities(userConfig.sourceGeneration.entities, classLoader)
      processConstants(userConfig.sourceGeneration.constants, classLoader)
    }
  }

  private void configureHeliumTask(BaseHeliumTask task, File output, ClassLoader classLoader) {
    task.output = output
    task.input = specification
    task.classLoader = classLoader
  }

  private void processEntities(SourceGenDslDelegate.EntitiesDslDelegate entities, ClassLoader classLoader) {
    if (!entities) {
      return
    }

    if (!entities.output) {
      entities.output = new File(userConfig.project.buildDir, "source/gen/rest-api")
    }
    GenerateJavaEntitiesTask task = userConfig.project.tasks.create(
        taskName("generateEntities", entities.genOptions),
        GenerateJavaEntitiesTask
    )
    configureHeliumTask(task, entities.output, classLoader)
    task.options = entities.genOptions
    config.sourceGen.entities[entities.genOptions.packageName] = task
  }

  private void processConstants(SourceGenDslDelegate.ConstantsDslDelegate constants, ClassLoader classLoader) {
    if (!constants) {
      return
    }

    if (!constants.output) {
      constants.output = new File(userConfig.project.buildDir, "source/gen/constants")
    }
    GenerateJavaConstantsTask task = userConfig.project.tasks.create(
        taskName("generateConstants", constants.genOptions),
        GenerateJavaConstantsTask
    )
    configureHeliumTask(task, constants.output, classLoader)
    task.options = constants.genOptions
    config.sourceGen.constants[constants.genOptions.packageName] = task
  }

  private static String taskName(final String prefix, final JavaGeneratorOptions options) {
    String pkgSuffix = Names.prettifiedName(Names.canonicalName(options.packageName))
    return "$prefix${pkgSuffix.capitalize()}"
  }

}
