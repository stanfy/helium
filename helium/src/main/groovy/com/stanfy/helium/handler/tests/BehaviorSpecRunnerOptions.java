package com.stanfy.helium.handler.tests;

/**
 * Options for behaviour spec runner.
 */
public final class BehaviorSpecRunnerOptions {

  boolean logToStdOut;
  boolean junitReport = true;
  boolean htmlReport = true;

  public BehaviorSpecRunnerOptions logToStdOut(final boolean value) {
    this.logToStdOut = value;
    return this;
  }

  public BehaviorSpecRunnerOptions junitReport(final boolean value) {
    this.junitReport = value;
    return this;
  }

  public BehaviorSpecRunnerOptions htmlReport(final boolean value) {
    this.htmlReport = value;
    return this;
  }

}
