package com.stanfy.helium.handler.codegen.java.entity;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.java.BaseJavaGenerator;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.constraints.ConstrainedType;
import com.stanfy.helium.model.constraints.EnumConstraint;
import com.stanfy.helium.utils.Names;

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
    EntitiesGeneratorOptions options = getOptions();

    for (Type type : project.getTypes().all()) {
      if (!options.isTypeIncluded(type)) {
        continue;
      }

      String className = Names.capitalize(type.getCanonicalName());
      File classFile = new File(targetDirectory, className.concat(EXT_JAVA));

      if (options.isTypeUserDefinedMessage(type)) {
        // turn message into a class
        write((Message) type, classFile);
      } else if (options.isEnumDeclaration(type)) {
        EnumConstraint<?> enumConstraint = (EnumConstraint<?>) ((ConstrainedType) type)
            .getConstraint(EnumConstraint.class);
        writeEnum(enumConstraint, type, classFile);
      }
    }
  }

  private void write(final Message type, final File classFile) {
    OutputStreamWriter output = null;
    try {
      output = new OutputStreamWriter(new FileOutputStream(classFile), "UTF-8");
      JavaClassWriter coreWriter = Writers.pojo().create(output);
      EntitiesGeneratorOptions options = getOptions();
      // TODO external parents check here
      new MessageToJavaClass(options.getWriterWrapper().wrapWriter(coreWriter, options), options).write(type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }


  @SuppressWarnings("unchecked")
  private void writeEnum(final EnumConstraint<?> constraint, final Type type, final File classFile) {
    EnumConstraint<String> enumConst = (EnumConstraint<String>) constraint;
    OutputStreamWriter output = null;
    try {
      output = new OutputStreamWriter(new FileOutputStream(classFile), "UTF-8");
      new ConstraintsToEnum(getOptions()).write(type, enumConst, output);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }

}
