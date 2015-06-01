package com.stanfy.helium.handler.tests;

import com.stanfy.helium.model.tests.BehaviourCheck;
import com.stanfy.helium.model.tests.BehaviourSuite;
import com.stanfy.helium.model.tests.CheckListener;
import okio.BufferedSink;

import java.io.Closeable;
import java.io.IOException;

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.FAILED;

/**
 * Logs spec execution progress and HTTP details.
 */
final class BehaviourLogger implements HeliumTestLog, CheckListener, Closeable {

  private static final String INDENT = "  ";

  private final BufferedSink output;
  private int indentLevel;

  BehaviourLogger(final BufferedSink output) {
    this.output = output;
  }

  private void push() {
    indentLevel++;
  }

  private void pop() {
    indentLevel--;
    if (indentLevel < 0) {
      throw new IllegalStateException();
    }
  }

  private void indent() throws IOException {
    for (int i = 0; i < indentLevel; i++) {
      output.writeUtf8(INDENT);
    }
  }

  private void log(final String msg, final boolean nl) {
    try {
      indent();
      output.writeUtf8(msg);
      if (nl) {
        output.writeUtf8("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(final String fmt, final Object... args) {
    log(args.length == 0 ? fmt : String.format(fmt, args), true);
  }

  @Override
  public void onSuiteStarted(final BehaviourSuite suite) {
    write("START %s", suite.getName());
    push();
  }

  @Override
  public void onCheckStarted(final BehaviourCheck check) {
    log(check.getName().concat("... "), false);
    push();
  }

  @Override
  public void onCheckDone(final BehaviourCheck check) {
    try {
      output.writeUtf8(check.getResult().toString()).writeUtf8("\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    pop();
    logErrors(check);
    write("\n");
  }

  private void logErrors(final BehaviourCheck check) {
    if (check.getResult() == FAILED && check.getDescription() != null) {
      write(check.getDescription());
    }
  }

  @Override
  public void onSuiteDone(final BehaviourSuite suite) {
    pop();
    write("%s (%d s)", suite.getResult(), suite.getTime().getStandardSeconds());
    logErrors(suite);
    write("\n");
  }

  @Override
  public void close() throws IOException {
    output.close();
  }
}
