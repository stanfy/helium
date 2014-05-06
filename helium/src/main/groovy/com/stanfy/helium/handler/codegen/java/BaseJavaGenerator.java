package com.stanfy.helium.handler.codegen.java;

import com.stanfy.helium.utils.Names;

import java.io.File;

/**
 * Base class for Java generators.
 */
public abstract class BaseJavaGenerator<T extends JavaGeneratorOptions> {

  /** Java file extension. */
  public static final String EXT_JAVA = ".java";
  /** Options instance. */
  private final T options;

  /** Output directory. */
  private final File outputDirectory;

  public BaseJavaGenerator(final File outputDirectory, final T options) {
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

  protected File getPackageDirectory() {
    File targetDirectory = new File(outputDirectory, Names.packageNameToPath(options.getPackageName()));
    if (!targetDirectory.mkdirs() && !targetDirectory.exists()) {
      throw new IllegalStateException("Cannot create directory " + targetDirectory);
    }
    return targetDirectory;
  }

}
