package com.stanfy.helium.handler.tests;

/**
 * Options for behaviour spec runner.
 */
public final class BehaviorSpecRunnerOptions {

  static final String DIR_JUNIT = "junit";

  boolean logToStdOut;
  boolean junitReport = true;

  public BehaviorSpecRunnerOptions logToStdOut(final boolean value) {
    this.logToStdOut = value;
    return this;
  }

  public BehaviorSpecRunnerOptions junitReport(final boolean value) {
    this.junitReport = value;
    return this;
  }

}
