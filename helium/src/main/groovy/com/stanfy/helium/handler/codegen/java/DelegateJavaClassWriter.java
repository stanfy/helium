package com.stanfy.helium.handler.codegen.java;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Delegate decorator.
 */
public class DelegateJavaClassWriter implements JavaClassWriter {

  /** Instance to delegate to. */
  private final JavaClassWriter core;

  public DelegateJavaClassWriter(final JavaClassWriter core) {
    this.core = core;
  }


  @Override
  public void writeImports(final Set<String> imports) throws IOException {
    core.writeImports(imports);
  }

  @Override
  public void writeClassBegin(final Message message, final String extending, final String... implementing) throws IOException {
    core.writeClassBegin(message, extending, implementing);
  }

  @Override
  public void writeClassEnd(final Message message) throws IOException {
    core.writeClassEnd(message);
  }

  @Override
  public void writeField(final Field field, final String fieldTypeName, final String fieldName, final Set<Modifier> modifiers) throws IOException {
    core.writeField(field, fieldTypeName, fieldName, modifiers);
  }

  @Override
  public void writeGetterMethod(final Field field, final String fieldTypeName, final String accessorName, final String fieldName) throws IOException {
    core.writeGetterMethod(field, fieldTypeName, accessorName, fieldName);
  }

  @Override
  public void writeSetterMethod(final Field field, final String fieldTypeName, final String accessorName, final String fieldName) throws IOException {
    core.writeSetterMethod(field, fieldTypeName, accessorName, fieldName);
  }

  @Override
  public JavaWriter getOutput() {
    return core.getOutput();
  }

  @Override
  public void writeConstructors(final Message message) throws IOException {
    core.writeConstructors(message);
  }

}
