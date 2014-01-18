package com.stanfy.helium.handler.codegen.java.constants;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Writes a constants file.
 */
public class MessageToConstants {

  /** Output. */
  private final JavaWriter output;

  /** Options. */
  private final ConstantsGeneratorOptions options;

  public MessageToConstants(final Writer output, final ConstantsGeneratorOptions options) {
    this.output = new JavaWriter(output);
    this.options = options;
  }


  public void write(final Message message) throws IOException {
    output.emitPackage(options.getPackageName());
    output.beginType(message.getCanonicalName() + "Constants", "class", Collections.singleton(Modifier.PUBLIC));

    for (Field field : message.getActiveFields()) {
      String name = options.getNameConverter().constantFrom(field);
      output.emitField(
          String.class.getCanonicalName(),
          name,
          EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL),
          "\"" + field.getCanonicalName() + "\""
      );
    }

    output.endType();
  }


}
