package com.stanfy.helium.handler.tests;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.stanfy.helium.model.tests.BehaviourCheck;
import com.stanfy.helium.model.tests.BehaviourSuite;
import com.stanfy.helium.model.tests.CheckListener;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class HtmlRenderer extends HeliumTestLogMemory implements CheckListener {

  private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory();
  private static final Mustache SUITE = MUSTACHE_FACTORY.compile("test-html-reports/suite.html");

  private ArrayList<BehaviourCheck> checksStack = new ArrayList<BehaviourCheck>();

  static void startHtmlReport(final BufferedSink sink) throws IOException {
    writeText(sink, "header");
  }

  static void endHtmlReport(final BufferedSink sink, final File targetDir) throws IOException {
    writeText(sink, "footer");
    BufferedSink stylesTarget = Okio.buffer(Okio.sink(new File(targetDir, "styles.css")));
    try {
      writeText(stylesTarget, "styles.css");
    } finally {
      stylesTarget.close();
    }
  }

  private static void writeText(final BufferedSink sink, final String name) throws IOException {
    BufferedSource src = Okio.buffer(Okio.source(HtmlRenderer.class.getResourceAsStream("/test-html-reports/" + name)));
    try {
      sink.writeAll(src);
    } finally {
      src.close();
    }
  }

  private static void mustache(final Mustache mustache, final Object scope, final BufferedSink sink)
      throws IOException {
    Writer out = new OutputStreamWriter(new NoCloseOutputStream(sink.outputStream()), "UTF-8");
    try {
      mustache.execute(out, scope);
    } finally {
      out.close();
    }
  }

  private List<ResultsTreeScope> children(final BehaviourSuite suite, final boolean failedOnly) {
    if (suite.getChildren().isEmpty()) {
      return null;
    }
    ArrayList<ResultsTreeScope> children = new ArrayList<ResultsTreeScope>(suite.getChildren().size());
    for (BehaviourCheck check : suite.getChildren()) {
      if (failedOnly && check.getResult() != BehaviourCheck.Result.FAILED) {
        continue;
      }
      List<ResultsTreeScope> childResults = Collections.emptyList();
      if (check instanceof BehaviourSuite) {
        childResults = children((BehaviourSuite) check, failedOnly);
      }
      children.add(new ResultsTreeScope(check, childResults, log(check)));
    }
    return children;
  }

  SuiteScope buildScope(final BehaviourSuite suite) {
    SuiteScope scope = new SuiteScope(suite);
    String rootLog = log(suite);
    if (rootLog.length() > 0) {
      scope.children.get(0).prependDetails(rootLog);
      if (scope.failedChildren != null) {
        scope.failedChildren.get("children").get(0).prependDetails(rootLog);
      }
    }
    return scope;
  }

  public void renderTo(final BehaviourSuite suite, final BufferedSink sink) throws IOException {
    mustache(SUITE, buildScope(suite), sink);
  }

  @Override
  public void onSuiteStarted(final BehaviourSuite suite) {
    checksStack.add(suite);
  }
  @Override
  public void onCheckStarted(final BehaviourCheck check) {
    checksStack.add(check);
  }
  @Override
  public void onCheckDone(final BehaviourCheck check) {
    checksStack.remove(checksStack.size() - 1);
  }
  @Override
  public void onSuiteDone(final BehaviourSuite suite) {
    checksStack.remove(checksStack.size() - 1);
  }

  @Override
  protected BehaviourCheck currentCheck() {
    return checksStack.isEmpty() ? null : checksStack.get(checksStack.size() - 1);
  }

  /** Template data object. */
  final class SuiteScope {

    String name;
    List<ResultsTreeScope> children;
    Map<String, List<ResultsTreeScope>> failedChildren;
    int totalCount;
    int failuresCount;

    SuiteScope(final BehaviourSuite suite) {
      this.name = suite.getName();
      count(suite);
      this.children = children(suite, false);
      List<ResultsTreeScope> failures = children(suite, true);
      this.failedChildren = failures == null || failures.isEmpty()
          ? null : Collections.singletonMap("children", failures);
    }

    private void count(final BehaviourCheck check) {
      if (check instanceof BehaviourSuite) {
        for (BehaviourCheck child : ((BehaviourSuite) check).getChildren()) {
          count(child);
        }
      } else {
        totalCount++;
        if (check.getResult() == BehaviourCheck.Result.FAILED) {
          failuresCount++;
        }
      }
    }

    @Override
    public String toString() {
      return "[" + name + ", " + children + "]";
    }
  }

  static final class ResultsTreeScope {
    final String title;
    final boolean failed;
    final boolean pending;
    final boolean success;
    final String status;
    final List<ResultsTreeScope> children;
    final String description;
    boolean hasDetails;
    String details;

    ResultsTreeScope(final BehaviourCheck check, final List<ResultsTreeScope> children, final String details) {
      title = check.getName();
      failed = check.getResult() == BehaviourCheck.Result.FAILED;
      pending = check.getResult() == BehaviourCheck.Result.PENDING;
      success = check.getResult() == BehaviourCheck.Result.PASSED;
      status = check.getResult().toString().toLowerCase(Locale.US);
      description = check.getDescription();
      this.children = children;
      this.hasDetails = details != null && details.length() > 0;
      this.details = details;
    }

    void prependDetails(final String log) {
      this.hasDetails = true;
      this.details = log + details;
    }

    @Override
    public String toString() {
      return "[" + title + ", " + status + ", d=" + details.length() + ", " + children + "]";
    }
  }

  private static class NoCloseOutputStream extends FilterOutputStream {

    public NoCloseOutputStream(final OutputStream out) {
      super(out);
    }

    @Override
    public void write(final byte[] b) throws IOException {
      out.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
      out.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
      // Nothing.
    }
  }

}
