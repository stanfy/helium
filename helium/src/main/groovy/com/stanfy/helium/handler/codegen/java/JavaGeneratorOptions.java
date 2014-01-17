package com.stanfy.helium.handler.codegen.java;

/**
 * Base class for Java generator options.
 */
public abstract class JavaGeneratorOptions {

  /** Package name for generated classes. */
  private String packageName;

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(final String packageName) {
    if (packageName == null) {
      throw new IllegalArgumentException("Package name cannot be null");
    }
    this.packageName = packageName;
  }

}
