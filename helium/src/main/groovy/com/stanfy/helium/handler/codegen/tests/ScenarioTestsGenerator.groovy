package com.stanfy.helium.handler.codegen.tests

import com.squareup.javawriter.JavaWriter
import com.stanfy.helium.DefaultType
import com.stanfy.helium.dsl.scenario.ScenarioDelegate
import com.stanfy.helium.dsl.scenario.ScenarioExecutor
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.tests.Scenario
import com.stanfy.helium.utils.DslUtils
import groovy.transform.CompileStatic

import javax.lang.model.element.Modifier

/**
 * Generator for scenario tests.
 */
@CompileStatic
public class ScenarioTestsGenerator extends BaseUnitTestsGenerator {

  /** Scenarios file. */
  private final File scenariosFile;

  public ScenarioTestsGenerator(final File scenariosFile, final File srcOutput) {
    this(scenariosFile, srcOutput, null);
  }
  public ScenarioTestsGenerator(final File scenariosFile, final File srcOutput, final File resourcesOutput) {
    this(scenariosFile, srcOutput, resourcesOutput, null);
  }
  public ScenarioTestsGenerator(final File scenariosFile, final File srcOutput, final File resourcesOutput, final String packageName) {
    super(srcOutput, resourcesOutput, packageName);
    if (!scenariosFile.exists()) {
      throw new IllegalArgumentException("Scenarios file does not exist")
    }

    this.scenariosFile = scenariosFile;
  }

  @Override
  protected String getClassName(final Service service) {
    return "${service.canonicalName}ScenariosTest"
  }

  @Override
  public void handle(final Project project) {
    // copy scenarios file
    specFile.withWriter(UTF_8) { Writer out ->
      DefaultType.values().each { DefaultType type ->
        out << "type '${type.langName}'\n"
      }
      out << scenariosFile.getText(UTF_8)
    }

    def toDelete = []
    eachService(project, { Service service, JavaWriter writer ->
      service.testInfo.scenarios.each { Scenario scenario ->
        addTestMethod service, scenario, writer
      }
      if (!service.testInfo.scenarios) {
        toDelete += service
      }
    } as BaseUnitTestsGenerator.ServiceHandler)
    toDelete.each { Service service ->
      getTestFile(getClassName(service)).delete()
    }

  }

  @Override
  protected void startTest(final JavaWriter writer, final Service service) throws IOException {
    super.startTest(writer, service)
    writer.emitField(Service.name, "service")
    writer.emitField("Object", "proxy")

    writer.beginMethod(null, getClassName(service), PUBLIC)
    writer.emitStatement("super()")
    writer.emitStatement("this.proxy = new ${ScenarioDelegate.name}(service, createExecutor())")
    writer.endMethod()
    writer.emitEmptyLine()

    writer.beginMethod(Project.name, "loadDefaultTestSpec", PROTECTED)
    writer.emitStatement("${Project.name} project = super.loadDefaultTestSpec()")
    writer.emitStatement("this.service = project.serviceByName(%s)", JavaWriter.stringLiteral(service.name))
    writer.emitStatement("return project")
    writer.endMethod()
    writer.emitEmptyLine()
  }

  private static void addTestMethod(final Service service, final Scenario scenario, final JavaWriter writer) {
    if (!scenario.action) {
      throw new IllegalStateException("Scenario '$scenario.name' has no action")
    }
    writer.emitAnnotation("Test")
    writer.beginMethod("void", scenario.canonicalName, Collections.<Modifier>singleton(Modifier.PUBLIC))

    writer.emitStatement("${Scenario.name} scenario = service.getTestInfo().scenarioByName(%s)", JavaWriter.stringLiteral(scenario.name))

    if (scenario.before) {
      writer.emitStatement("${DslUtils.name}.runWithProxy(proxy, scenario.getBefore())")
    }
    writer.emitStatement("${DslUtils.name}.runWithProxy(proxy, scenario.getAction())")
    if (scenario.after) {
      writer.emitStatement("${DslUtils.name}.runWithProxy(proxy, scenario.getAfter())")
    }

    writer.endMethod()
    writer.emitEmptyLine()
  }

}
