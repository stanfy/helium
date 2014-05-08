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
import static com.stanfy.helium.gradle.UserConfig.specName

@PackageScope
final class HeliumInitializer implements TasksCreator {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(HeliumInitializer.class)

  private static final String TESTS_OUT_PATH = "helium/api-tests"

  /** Config object. */
  private final HeliumExtension config
  /** User configuration. */
  private final UserConfig userConfig;

  public HeliumInitializer(final HeliumExtension config, final UserConfig userConfig) {
    this.config = config
    this.userConfig = userConfig
  }

  @Override
  void createTasks(ClassLoader classLoader) {
    config.specifications.each {
      // runApiTest
      createApiTestTasks(it, classLoader)

      // source generation
      SourceGenDslDelegate sourceGen = userConfig.getSourceGenFor(it)
      if (sourceGen != null) {
        processEntities(sourceGen.entities, classLoader, it)
        processConstants(sourceGen.constants, classLoader, it)
      }
    }
  }

  private String taskName(final String prefix, final File specification) {
    if (config.specifications.size() > 1) {
      return "${prefix}${specName(specification).capitalize()}"
    }
    return prefix
  }

  private void createApiTestTasks(final File specification, final ClassLoader classLoader) {
    Project project = userConfig.project

    // tests generation task
    GenerateApiTestsTask genTestsTask = project.tasks.create(taskName("genApiTests", specification), GenerateApiTestsTask)
    genTestsTask.group = GROUP
    configureHeliumTask(genTestsTask, specification, new File(project.buildDir, "source/$TESTS_OUT_PATH"), classLoader)
    LOG.debug "genApiTests task: json=$genTestsTask.input, output=$genTestsTask.output"

    // tests run task
    GradleBuild runTestsTask = project.tasks.create(taskName('runApiTests', specification), GradleBuild)
    runTestsTask.group = GROUP
    runTestsTask.buildFile = new File(genTestsTask.output, "build.gradle")
    runTestsTask.dir = genTestsTask.output
    runTestsTask.tasks = ['check']
    runTestsTask.dependsOn genTestsTask
    LOG.debug "runApiTests task: dir=$runTestsTask.dir"
  }

  private void configureHeliumTask(BaseHeliumTask task, File specification, File output, ClassLoader classLoader) {
    task.output = output
    task.input = specification
    task.classLoader = classLoader
  }

  private void processEntities(SourceGenDslDelegate.EntitiesDslDelegate entities, ClassLoader classLoader,
                               File specification) {
    if (!entities) {
      return
    }

    if (!entities.output) {
      entities.output = new File(userConfig.project.buildDir, "source/gen/rest-api")
    }
    GenerateJavaEntitiesTask task = userConfig.project.tasks.create(
        taskName("generateEntities", specification, entities.genOptions),
        GenerateJavaEntitiesTask
    )
    configureHeliumTask(task, specification, entities.output, classLoader)
    task.options = entities.genOptions
    config.sourceGen(specification).entities[entities.genOptions.packageName] = task
  }

  private void processConstants(SourceGenDslDelegate.ConstantsDslDelegate constants, ClassLoader classLoader,
                                File specification) {
    if (!constants) {
      return
    }

    if (!constants.output) {
      constants.output = new File(userConfig.project.buildDir, "source/gen/constants")
    }
    GenerateJavaConstantsTask task = userConfig.project.tasks.create(
        taskName("generateConstants", specification, constants.genOptions),
        GenerateJavaConstantsTask
    )
    configureHeliumTask(task, specification, constants.output, classLoader)
    task.options = constants.genOptions
    config.sourceGen(specification).constants[constants.genOptions.packageName] = task
  }

  private String taskName(final String prefix, final File specification, final JavaGeneratorOptions options) {
    String pkgSuffix = Names.prettifiedName(Names.canonicalName(options.packageName))
    return "${taskName(prefix, specification)}${pkgSuffix.capitalize()}"
  }

}
