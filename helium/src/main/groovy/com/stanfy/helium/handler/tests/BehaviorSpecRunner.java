package com.stanfy.helium.handler.tests;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.model.Checkable;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.TypeResolver;
import com.stanfy.helium.model.tests.BehaviourCheck;
import com.stanfy.helium.model.tests.BehaviourSuite;
import com.stanfy.helium.model.tests.CheckListener;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED;

/**
 * Runs behaviour specs defined in the project and generates reports.
 */
public class BehaviorSpecRunner implements Handler {

  private static final String JUNIT = "junit";

  private final BehaviorSpecRunnerOptions options;
  private final File output;

  private boolean passed;
  private JUnitReportGenerator junit;

  public BehaviorSpecRunner(final BehaviorSpecRunnerOptions options, final File output) {
    if (!output.isDirectory()) {
      throw new IllegalArgumentException(output + " is not a directory or does not exist");
    }
    this.options = options;
    this.output = output;
  }

  private static BufferedSink stdout() {
    return Okio.buffer(Okio.sink(System.out));
  }

  private ComposedListener buildListener(final String name, final boolean firstRun) {
    ArrayList<CheckListener> listeners = new ArrayList<CheckListener>();
    if (options.logToStdOut) {
      listeners.add(new BehaviourLogger(stdout()));
    }
    if (options.junitReport) {
      junit = new JUnitReportGenerator();
      listeners.add(junit);
    }
    try {
      Sink sink = firstRun ? Okio.sink(outputLog()) : Okio.appendingSink(outputLog());
      listeners.add(new BehaviourLogger(Okio.buffer(sink)));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    return new ComposedListener(listeners);
  }

  private BehaviourSuite run(final Checkable checkable, final String name, final TypeResolver types,
                             final boolean firstRun) {
    ComposedListener listener = buildListener(name, firstRun);
    HeliumTest test = new HeliumTest(listener);
    HttpExecutor executor = new HttpExecutor(types, test.httpClient());
    try {
      BehaviourSuite result = checkable.check(executor, listener);
      if (junit != null && !result.getChildren().isEmpty()) {
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
      return result;
    } finally {
      IOUtils.closeQuietly(listener);
    }
  }

  @Override
  public void handle(final Project project) {
    TypeResolver types = project.getTypes();
    BehaviourSuite projectResult = run(project, "project", types, true);
    passed = projectResult.getResult() != FAILED;
    for (Service service : project.getServices()) {
      BehaviourSuite serviceResult = run(service, service.getName(), types, false);
      passed &= serviceResult.getResult() != FAILED;
    }
  }

  public File outputLog() {
    return new File(output, "behaviour-checks.log");
  }

  public boolean passed() {
    return passed;
  }

  private static class ComposedListener implements CheckListener, HeliumTestLog, Closeable {
    private final Collection<CheckListener> listeners;

    ComposedListener(final Collection<CheckListener> listeners) {
      this.listeners = listeners;
    }


    @Override
    public void onSuiteStarted(final BehaviourSuite suite) {
      for (CheckListener l : listeners) {
        l.onSuiteStarted(suite);
      }
    }

    @Override
    public void onCheckStarted(final BehaviourCheck check) {
      for (CheckListener l : listeners) {
        l.onCheckStarted(check);
      }
    }

    @Override
    public void onCheckDone(final BehaviourCheck check) {
      for (CheckListener l : listeners) {
        l.onCheckDone(check);
      }
    }

    @Override
    public void onSuiteDone(final BehaviourSuite suite) {
      for (CheckListener l : listeners) {
        l.onSuiteDone(suite);
      }
    }

    @Override
    public void write(final String fmt, final Object... args) {
      for (CheckListener l : listeners) {
        if (l instanceof HeliumTestLog) {
          ((HeliumTestLog) l).write(fmt, args);
        }
      }
    }

    @Override
    public void close() throws IOException {
      for (CheckListener l : listeners) {
        if (l instanceof Closeable) {
          ((Closeable) l).close();
        }
      }
    }
  }

}
