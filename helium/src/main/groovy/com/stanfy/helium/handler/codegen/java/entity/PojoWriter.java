package com.stanfy.helium.handler.codegen.java.entity;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Writes type as a Java class.
 */
class PojoWriter implements JavaClassWriter {

  /** Output. */
  private final JavaWriter output;

  public PojoWriter(final Writer output) {
    this.output = new JavaWriter(output);
  }

  @Override
  public void writeImports(final Set<String> imports) throws IOException {
    if (!imports.isEmpty()) {
      output.emitImports(imports);
      output.emitEmptyLine();
    }
  }

  @Override
  public void writeClassBegin(final Message message, final String extending, final String... implementing) throws IOException {
    output.beginType(message.getCanonicalName(), "class", Collections.singleton(Modifier.PUBLIC), extending, implementing);
  }

  @Override
  public void writeField(final Field field, final String fieldTypeName, final String fieldName, final Set<Modifier> modifiers) throws IOException {
    output.emitField(fieldTypeName, fieldName, modifiers);
  }

  @Override
  public void writeSetterMethod(final Field field, final String fieldTypeName, final String accessorName, final String fieldName) throws IOException {
    output.beginMethod("void", accessorName, Collections.singleton(Modifier.PUBLIC), fieldTypeName, "value");
    output.emitStatement("this.%s = value", fieldName);
    output.endMethod();
  }

  @Override
  public void writeGetterMethod(final Field field, final String fieldTypeName, final String accessorName, final String fieldName) throws IOException {
    output.beginMethod(fieldTypeName, accessorName, Collections.singleton(Modifier.PUBLIC));
    output.emitStatement("return this.%s", fieldName);
    output.endMethod();
  }

  @Override
  public void writeClassEnd(final Message message) throws IOException {
    output.endType();
  }

  @Override
  public JavaWriter getOutput() {
    return output;
  }

  @Override
  public void writeConstructors(final Message message) {
    // nothing
  }

}
