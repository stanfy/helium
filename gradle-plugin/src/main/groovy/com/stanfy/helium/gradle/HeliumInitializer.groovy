package com.stanfy.helium.gradle

import com.stanfy.helium.Helium
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
  private Helium heliumInstance

  public HeliumInitializer(final HeliumExtension config, final Config userConfig) {
    this.config = config
    this.userConfig = userConfig
  }

  @Override
  void createTasks() {
    Project project = userConfig.project
    heliumInstance = new Helium().defaultTypes()
    specification = config.specification
    if (specification) {
      heliumInstance.from specification
    }

    if (specification) {
      // tests generation task
      GenerateApiTestsTask genTestsTask = project.tasks.create("genApiTests", GenerateApiTestsTask)
      genTestsTask.group = GROUP
      genTestsTask.output = new File(project.buildDir, "source/$TESTS_OUT_PATH")
      genTestsTask.input = specification
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
      processEntities(userConfig.sourceGeneration.entities)
      processConstants(userConfig.sourceGeneration.constants)
    }
  }

  private void configureGenTask(BaseHeliumTask task, File output) {
    task.output = output
    task.input = specification
    task.helium = heliumInstance
  }

  private void processEntities(SourceGenDslDelegate.EntitiesDslDelegate entities) {
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
    configureGenTask(task, entities.output)
    task.options = entities.genOptions
    config.sourceGen.entities[entities.genOptions.packageName] = task
  }

  private void processConstants(SourceGenDslDelegate.ConstantsDslDelegate constants) {
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
    configureGenTask(task, constants.output)
    task.options = constants.genOptions
    config.sourceGen.constants[constants.genOptions.packageName] = task
  }

  private static String taskName(final String prefix, final JavaGeneratorOptions options) {
    String pkgSuffix = Names.prettifiedName(Names.canonicalName(options.packageName))
    return "$prefix${pkgSuffix.capitalize()}"
  }

}
