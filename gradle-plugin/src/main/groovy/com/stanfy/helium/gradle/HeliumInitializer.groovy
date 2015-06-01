package com.stanfy.helium.gradle

import com.stanfy.helium.gradle.tasks.BaseHeliumTask
import com.stanfy.helium.gradle.tasks.GenerateApiTestsTask
import com.stanfy.helium.gradle.tasks.RunBehaviourSpecsTask
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
  private static final String SPEC_CHECK_OUT_PATH = "reports/helium"

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
      def specCheckTask = runBehaviourSpecsTask(it, classpath)
      if (runApiTest) {
        runApiTest.dependsOn runTask
        runApiTest.dependsOn specCheckTask
      }

      // source generation
      SourceGenDslDelegate sourceGen = userConfig.getSourceGenFor(it)
      if (sourceGen != null) {
        sourceGen.createTasks(userConfig, it, classpath, BASE_OUT_PATH, config)
      }
    }
  }

  public static String taskName(final String prefix, final File specification, final HeliumExtension config) {
    if (config.specifications.size() > 1) {
      return "${prefix}${specName(specification).capitalize()}"
    }
    return prefix
  }

  private GradleBuild createApiTestTasks(final File specification, final URL[] classpath) {
    Project project = userConfig.project

    def specName = specName(specification)

    // tests generation task
    GenerateApiTestsTask genTestsTask = project.tasks.create(taskName("genApiTests", specification, config), GenerateApiTestsTask)
    genTestsTask.group = GROUP
    genTestsTask.description = "Generate project with API tests for specification '$specName'"
    def testsProjectDir = new File(project.buildDir, "$TESTS_OUT_PATH/$specName")
    configureHeliumTask(genTestsTask, specification, testsProjectDir, classpath, userConfig)
    LOG.debug "genApiTests task: json=$genTestsTask.input, output=$genTestsTask.output"

    // tests run task
    GradleBuild runTestsTask = project.tasks.create(taskName('runApiTests', specification, config), GradleBuild)
    runTestsTask.group = GROUP
    runTestsTask.description = "Run API tests for specification '$specName'"
    runTestsTask.buildFile = new File(genTestsTask.output, "build.gradle")
    runTestsTask.dir = genTestsTask.output
    runTestsTask.tasks = ['check']
    runTestsTask.dependsOn genTestsTask
    LOG.debug "runApiTests task: dir=$runTestsTask.dir"

    return runTestsTask
  }

  private RunBehaviourSpecsTask runBehaviourSpecsTask(final File specification, final URL[] classpath) {
    Project project = userConfig.project
    def specName = specName(specification)
    def res = project.tasks.create(taskName("checkApiBehaviour", specification, config), RunBehaviourSpecsTask)
    res.configure {
      group = GROUP
      description = "Run API behaviour specifications for '$specName'"
    }
    def outputDir = new File(project.buildDir, SPEC_CHECK_OUT_PATH.concat("/$specName"))
    configureHeliumTask(res, specification, outputDir, classpath, userConfig)
    return res
  }

  public static void configureHeliumTask(BaseHeliumTask task, File specification, File output,
                                         URL[] classpath, UserConfig userConfig) {
    task.output = output
    task.input = specification
    task.classpath = classpath
    task.variables = Collections.unmodifiableMap(userConfig.variables)
  }

}
