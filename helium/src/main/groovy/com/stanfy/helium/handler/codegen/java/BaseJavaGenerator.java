package com.stanfy.helium.handler.codegen.java;

import com.stanfy.helium.handler.codegen.BaseGenerator;
import com.stanfy.helium.utils.Names;

import java.io.File;

/**
 * Base class for Java generators.
 */
public abstract class BaseJavaGenerator<T extends JavaGeneratorOptions> extends BaseGenerator<T> {
  /** Java file extension. */
  public static final String EXT_JAVA = ".java";

  public BaseJavaGenerator(final File outputDirectory, final T options) {
    super(outputDirectory, options);
  }

  protected File getPackageDirectory() {
    File targetDirectory = new File(getOutputDirectory(), Names.packageNameToPath(getOptions().getPackageName()));
    if (!targetDirectory.mkdirs() && !targetDirectory.exists()) {
      throw new IllegalStateException("Cannot create directory " + targetDirectory);
    }
    return targetDirectory;
  }
}
