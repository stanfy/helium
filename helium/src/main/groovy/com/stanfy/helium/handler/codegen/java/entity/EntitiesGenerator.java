package com.stanfy.helium.handler.codegen.java.entity;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.java.BaseJavaGenerator;
import com.stanfy.helium.handler.codegen.java.JavaGeneratorOptions;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Type;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * POJO generator.
 */
public class EntitiesGenerator extends BaseJavaGenerator<EntitiesGeneratorOptions> implements Handler {

  public EntitiesGenerator(final File outputDirectory, final EntitiesGeneratorOptions options) {
    super(outputDirectory, options);
  }

  public EntitiesGenerator(final File outputDirectory, final String packageName) {
    this(outputDirectory, EntitiesGeneratorOptions.defaultOptions(packageName));
  }

  @Override
  public void handle(final Project project) {
    File targetDirectory = getPackageDirectory();
    JavaGeneratorOptions options = getOptions();

    for (Type type : project.getTypes().all()) {
      boolean shouldProcess = options.isTypeUserDefinedMessage(type) && options.isTypeIncluded(type);
      if (shouldProcess) {
        File classFile = new File(targetDirectory, type.getCanonicalName().concat(EXT_JAVA));
        write((Message) type, classFile);
      }
    }
  }

  private void write(Message type, File classFile) {
    OutputStreamWriter output = null;
    try {
      output = new OutputStreamWriter(new FileOutputStream(classFile), "UTF-8");
      JavaClassWriter coreWriter = Writers.pojo().create(output);
      EntitiesGeneratorOptions options = getOptions();
      new MessageToJavaClass(options.getWriterWrapper().wrapWriter(coreWriter, options), options).write(type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }

}
