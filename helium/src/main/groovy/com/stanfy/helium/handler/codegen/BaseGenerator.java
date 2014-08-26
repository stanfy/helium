package com.stanfy.helium.handler.codegen;

import java.io.File;

/**
 * Base class for generators.
 */
public abstract class BaseGenerator<T extends GeneratorOptions> {

  /** Options instance. */
  private final T options;

  /** Output directory. */
  private final File outputDirectory;

  public BaseGenerator(final File outputDirectory, final T options) {
    if (outputDirectory == null) {
      throw new IllegalArgumentException("Output directory cannot be null");
    }
    if (options == null) {
      throw new IllegalArgumentException("Options cannot be null");
    }

    this.options = options;
    this.outputDirectory = outputDirectory;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public T getOptions() {
    return options;
  }

}
