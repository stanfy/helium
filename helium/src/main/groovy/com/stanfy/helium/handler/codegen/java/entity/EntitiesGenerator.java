package com.stanfy.helium.handler.codegen.java.entity;

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
public class EntitiesGenerator implements Handler {

  /** Output directory. */
  private final File outputDirectory;

  /** Options. */
  private final EntitiesGeneratorOptions options;

  public EntitiesGenerator(final File outputDirectory, final EntitiesGeneratorOptions options) {
    this.outputDirectory = outputDirectory;
    this.options = options;
  }

  public EntitiesGenerator(final File outputDirectory, final String packageName) {
    this.outputDirectory = outputDirectory;
    this.options = EntitiesGeneratorOptions.defaultOptions(packageName);
  }

  @Override
  public void handle(final Project project) {
    File targetDirectory = new File(outputDirectory, Names.packageNameToPath(options.getPackageName()));
    if (!targetDirectory.mkdirs() && !targetDirectory.exists()) {
      throw new IllegalStateException("Cannot create directory " + targetDirectory);
    }

    for (Type type : project.getTypes().all()) {
      boolean shouldProcess = options.isTypeUserDefinedMessage(type) && options.isTypeIncluded(type);
      if (shouldProcess) {
        File classFile = new File(targetDirectory, type.getCanonicalName() + ".java");
        write((Message) type, classFile);
      }
    }
  }

  private void write(Message type, File classFile) {
    OutputStreamWriter output = null;
    try {
      output = new OutputStreamWriter(new FileOutputStream(classFile), "UTF-8");
      JavaClassWriter coreWriter = Writers.pojo().create(output);
      new MessageToJavaClass(options.getWriterWrapper().wrapWriter(coreWriter, options), options).write(type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }

}
