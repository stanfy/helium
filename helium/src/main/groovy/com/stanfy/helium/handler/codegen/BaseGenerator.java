package com.stanfy.helium.handler.codegen;

import com.stanfy.helium.model.Service;

import java.io.File;

/**
 * Base class for generators.
 */
public abstract class BaseGenerator<T extends GeneratorOptions> {

  private static final String MISSING_SERVICE_NAME = "Please define your service name. "
      + "It is required to get source code generated. It should be something like\n"
      + "service {\n"
      + "  name 'YOUR SERVICE NAME HERE'\n"
      + "  // Continue with your methods...\n"
      + "}\n";

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

  public static void ensureServiceNamePresent(final Service service) {
    if (service.getName() == null || service.getName().trim().length() == 0) {
      throw new IllegalStateException(MISSING_SERVICE_NAME);
    }
  }

}
