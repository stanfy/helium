package com.stanfy.helium.handler.codegen.java.constants;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.utils.Names;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Generates constants from Messages field names.
 */
public class JavaConstantsGenerator implements Handler {

  /** Output directory. */
  private final File outputDirectory;

  /** Options. */
  private final ConstantsGeneratorOptions options;

  public JavaConstantsGenerator(final File outputDirectory, final ConstantsGeneratorOptions options) {
    if (outputDirectory == null) {
      throw new IllegalArgumentException("Output directory cannot be null");
    }
    if (options == null) {
      throw new IllegalArgumentException("Options cannot be null");
    }
    this.outputDirectory = outputDirectory;
    this.options = options;
  }

  @Override
  public void handle(final Project project) {
    File targetDirectory = new File(outputDirectory, Names.packageNameToPath(options.getPackageName()));
    if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
      throw new IllegalStateException("Output directory cannot be created");
    }

    for (Type type : project.getTypes().all()) {
      if (type instanceof Message) {
        File classFile = new File(targetDirectory, type.getCanonicalName() + "Constants.java");
        Writer output = null;
        try {
          output = new OutputStreamWriter(new FileOutputStream(classFile), "UTF-8");
          new MessageToConstants(output, options).write((Message) type);
        } catch (IOException e) {
          throw new RuntimeException(e);
        } finally {
          IOUtils.closeQuietly(output);
        }
      }
    }
  }

}
