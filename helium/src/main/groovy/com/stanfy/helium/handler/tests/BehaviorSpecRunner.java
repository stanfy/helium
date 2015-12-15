package com.stanfy.helium.handler.tests;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.model.Checkable;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.TypeResolver;
import com.stanfy.helium.model.tests.BehaviourSuite;
import com.stanfy.helium.model.tests.CheckListener;
import okio.BufferedSink;
import okio.Okio;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED;

/**
 * Runs behaviour specs defined in the project and generates reports.
 */
public class BehaviorSpecRunner implements Handler {

  private static final String JUNIT = "junit";
  private static final String HTML = "html";

  private final BehaviorSpecRunnerOptions options;
  private final File output;

  private boolean passed;

  private BufferedSink htmlOutput;
  private BufferedSink textOutput;

  private JUnitReportGenerator junit;
  private HtmlRenderer html;

  public BehaviorSpecRunner(final BehaviorSpecRunnerOptions options, final File output) {
    if (!output.isDirectory()) {
      throw new IllegalArgumentException(output + " is not a directory or does not exist");
    }
    this.options = options;
    this.output = output;
  }

  private ComposedCheckListener buildListener() {
    ArrayList<CheckListener> listeners = new ArrayList<CheckListener>();
    if (options.logToStdOut) {
      listeners.add(new BehaviourLogger(Okio.buffer(Okio.sink(System.out))));
    }
    if (options.junitReport) {
      junit = new JUnitReportGenerator();
      listeners.add(junit);
    }
    if (options.htmlReport) {
      html = new HtmlRenderer();
      listeners.add(html);
    }
    listeners.add(new BehaviourLogger(textOutput));
    return new ComposedCheckListener(listeners);
  }

  private BehaviourSuite run(final Checkable checkable, final TypeResolver types) {
    ComposedCheckListener listener = buildListener();
    HeliumTest test = new HeliumTest(listener);
    HttpExecutor executor = new HttpExecutor(types, test.httpClient());
    BehaviourSuite result = checkable.check(executor, listener);
    if (result.getChildren().isEmpty()) {
      return result;
    }
    // Save JUnit report.
    if (junit != null) {
      File junitDir = new File(output, JUNIT);
      junitDir.mkdirs();
      BufferedSink output = null;
      try {
        output = Okio.buffer(Okio.sink(new File(junitDir, result.getCanonicalName().concat(".xml"))));
        junit.generateInto(output, result);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        IOUtils.closeQuietly(output);
      }
    }
    // Save HTML report.
    if (options.htmlReport) {
      try {
        html.renderTo(result, htmlOutput);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  @Override
  public void handle(final Project project) {
    try {
      initOutputs();

      TypeResolver types = project.getTypes();
      BehaviourSuite projectResult = run(project, types);
      passed = projectResult.getResult() != FAILED;
      for (Service service : project.getServices()) {
        BehaviourSuite serviceResult = run(service, types);
        passed &= serviceResult.getResult() != FAILED;
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot initialize outputs", e);
    } finally {
      closeOutputs();
    }
  }

  private void closeOutputs() {
    if (htmlOutput != null) {
      try {
        HtmlRenderer.endHtmlReport(htmlOutput, new File(output, HTML));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    IOUtils.closeQuietly(textOutput);
    IOUtils.closeQuietly(htmlOutput);
  }

  private void initOutputs() throws IOException {
    textOutput = Okio.buffer(Okio.sink(outputLog()));
    if (options.htmlReport) {
      File dir = new File(output, HTML);
      dir.mkdirs();
      htmlOutput = Okio.buffer(Okio.sink(new File(dir, "index.html")));
      HtmlRenderer.startHtmlReport(htmlOutput);
    }
  }

  public File outputLog() {
    return new File(output, "behaviour-checks.log");
  }

  public boolean passed() {
    return passed;
  }

}
