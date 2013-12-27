package com.stanfy.helium.handler.codegen.java;

import com.google.gson.annotations.SerializedName;
import com.stanfy.helium.model.Field;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Adds {@code SerializedName} annotations.
 */
public class MessageToGsonClass extends MessageToJavaClass {

  public MessageToGsonClass(Writer output, PojoGeneratorOptions options) {
    super(output, options);
  }

  @Override
  protected void writeImports(final Set<String> imports) throws IOException {
    HashSet<String> newImports = new HashSet<String>(imports.size() + 1);
    newImports.add(SerializedName.class.getCanonicalName());
    super.writeImports(newImports);
  }

  @Override
  protected void writeField(final Field field) throws IOException {
    getOutput().emitAnnotation(SerializedName.class, "\"" + field.getName() + "\"");
    super.writeField(field);
  }

}
