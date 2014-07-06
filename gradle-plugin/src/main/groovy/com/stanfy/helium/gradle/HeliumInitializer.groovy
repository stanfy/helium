package com.stanfy.helium.gradle

import com.stanfy.helium.gradle.SourceGenDslDelegate.RetrofitDslDelegate
import com.stanfy.helium.gradle.tasks.*
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
final class HeliumInitializer {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(HeliumInitializer.class)

  private static final String BASE_OUT_PATH = "generated/source/helium"
  private static final String TESTS_OUT_PATH = "$BASE_OUT_PATH/api-tests"

  /** Config object. */
  private final HeliumExtension config
  /** User configuration. */
  private final UserConfig userConfig;

  public HeliumInitializer(final HeliumExtension config, final UserConfig userConfig) {
    this.config = config
    this.userConfig = userConfig
  }

  void createTasks(URL[] classpath) {
    def runApiTest = null
    if (config.specifications.size() > 1) {
      runApiTest = userConfig.project.tasks.create("runApiTests")
      runApiTest.description = "Run all API tests"
      runApiTest.group = GROUP
    }

    config.specifications.each {
      // runApiTest
      def runTask = createApiTestTasks(it, classpath)
      if (runApiTest) {
        runApiTest.dependsOn runTask
      }

      // source generation
      SourceGenDslDelegate sourceGen = userConfig.getSourceGenFor(it)
      if (sourceGen != null) {
        processEntities(sourceGen.entities, classpath, it)
        processConstants(sourceGen.constants, classpath, it)
        processRetrofit(sourceGen.retrofit, classpath, it)
      }
    }
  }

  private String taskName(final String prefix, final File specification) {
    if (config.specifications.size() > 1) {
      return "${prefix}${specName(specification).capitalize()}"
    }
    return prefix
  }

  private GradleBuild createApiTestTasks(final File specification, final URL[] classpath) {
    Project project = userConfig.project

    def specName = specName(specification)

    // tests generation task
    GenerateApiTestsTask genTestsTask = project.tasks.create(taskName("genApiTests", specification), GenerateApiTestsTask)
    genTestsTask.group = GROUP
    genTestsTask.description = "Generate project with API tests for specification '$specName'"
    def testsProjectDir = new File(project.buildDir, "$TESTS_OUT_PATH/$specName")
    configureHeliumTask(genTestsTask, specification, testsProjectDir, classpath)
    LOG.debug "genApiTests task: json=$genTestsTask.input, output=$genTestsTask.output"

    // tests run task
    GradleBuild runTestsTask = project.tasks.create(taskName('runApiTests', specification), GradleBuild)
    runTestsTask.group = GROUP
    runTestsTask.description = "Run API tests for specification '$specName'"
    runTestsTask.buildFile = new File(genTestsTask.output, "build.gradle")
    runTestsTask.dir = genTestsTask.output
    runTestsTask.tasks = ['check']
    runTestsTask.dependsOn genTestsTask
    LOG.debug "runApiTests task: dir=$runTestsTask.dir"

    return runTestsTask
  }

  private void configureHeliumTask(BaseHeliumTask task, File specification, File output,
                                   URL[] classpath) {
    task.output = output
    task.input = specification
    task.classpath = classpath
    task.variables = Collections.unmodifiableMap(userConfig.variables)
  }

  private void processEntities(SourceGenDslDelegate.EntitiesDslDelegate entities, URL[] classpath,
                               File specification) {
    if (!entities) {
      return
    }

    if (!entities.output) {
      entities.output = new File(userConfig.project.buildDir, "$BASE_OUT_PATH/entities/${specName(specification)}")
    }
    GenerateJavaEntitiesTask task = userConfig.project.tasks.create(
        taskName("generateEntities", specification, entities.genOptions),
        GenerateJavaEntitiesTask
    )
    configureHeliumTask(task, specification, entities.output, classpath)
    task.options = entities.genOptions
    config.sourceGen(specification).entities[entities.genOptions.packageName] = task
  }

  private void processConstants(SourceGenDslDelegate.ConstantsDslDelegate constants, URL[] classpath,
                                File specification) {
    if (!constants) {
      return
    }

    if (!constants.output) {
      constants.output = new File(userConfig.project.buildDir, "$BASE_OUT_PATH/constants/${specName(specification)}")
    }
    GenerateJavaConstantsTask task = userConfig.project.tasks.create(
        taskName("generateConstants", specification, constants.genOptions),
        GenerateJavaConstantsTask
    )
    configureHeliumTask(task, specification, constants.output, classpath)
    task.options = constants.genOptions
    config.sourceGen(specification).constants[constants.genOptions.packageName] = task
  }

  private void processRetrofit(RetrofitDslDelegate retrofit, URL[] classpath, File specification) {
    if (!retrofit) {
      return
    }

    if (!retrofit.output) {
      retrofit.output = new File(userConfig.project.buildDir, "$BASE_OUT_PATH/retrofit/${specName(specification)}")
    }
    GenerateRetrofitTask task = userConfig.project.tasks.create(
        taskName("generateRetrofit", specification, retrofit.genOptions),
        GenerateRetrofitTask
    )
    configureHeliumTask(task, specification, retrofit.output, classpath)
    task.options = retrofit.genOptions
    config.sourceGen(specification).retrofit[retrofit.genOptions.packageName] = task
  }

  private String taskName(final String prefix, final File specification, final JavaGeneratorOptions options) {
    String pkgSuffix = Names.prettifiedName(Names.canonicalName(options.packageName))
    return "${taskName(prefix, specification)}${pkgSuffix.capitalize()}"
  }

}
