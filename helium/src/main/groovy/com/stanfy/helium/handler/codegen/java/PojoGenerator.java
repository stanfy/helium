package com.stanfy.helium.handler.codegen.java;

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

/**
 * POJO generator.
 */
public class PojoGenerator implements Handler {

  /** Output directory. */
  private final File outputDirectory;

  /** Options. */
  private final PojoGeneratorOptions options;

  public PojoGenerator(final File outputDirectory, final PojoGeneratorOptions options) {
    this.outputDirectory = outputDirectory;
    this.options = options;
  }

  public PojoGenerator(final File outputDirectory, final String packageName) {
    this.outputDirectory = outputDirectory;
    this.options = PojoGeneratorOptions.defaultOptions(packageName);
  }

  @Override
  public void handle(final Project project) {
    File targetDirectory = new File(outputDirectory, Names.packageNameToPath(options.getPackageName()));
    if (!targetDirectory.mkdirs() && !targetDirectory.exists()) {
      throw new IllegalStateException("Cannot create directory " + targetDirectory);
    }

    for (Type type : project.getTypes().all()) {
      if (type instanceof Message) {
        File classFile = new File(targetDirectory, type.getCanonicalName() + ".java");
        write((Message) type, classFile);
      }
    }
  }

  private void write(Message type, File classFile) {
    OutputStreamWriter output = null;
    try {
      output = new OutputStreamWriter(new FileOutputStream(classFile), "UTF-8");
      MessageToJavaClass gen = new MessageToJavaClass(output, options);
      gen.write(type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }

}
