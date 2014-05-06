package com.stanfy.helium.handler.codegen.java.constants;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.java.BaseJavaGenerator;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Type;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Generates constants from Messages field names.
 */
public class JavaConstantsGenerator extends BaseJavaGenerator<ConstantsGeneratorOptions> implements Handler {

  public JavaConstantsGenerator(final File outputDirectory, final ConstantsGeneratorOptions options) {
    super(outputDirectory, options);
  }

  @Override
  public void handle(final Project project) {
    File targetDirectory = getPackageDirectory();
    ConstantsGeneratorOptions options = getOptions();

    for (Type type : project.getTypes().all()) {
      boolean shouldProcess = options.isTypeUserDefinedMessage(type) && options.isTypeIncluded(type);
      if (shouldProcess) {
        File classFile = new File(targetDirectory, type.getCanonicalName() + "Constants" + EXT_JAVA);
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
