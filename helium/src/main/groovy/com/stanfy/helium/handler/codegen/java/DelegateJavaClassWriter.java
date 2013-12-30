package com.stanfy.helium.handler.codegen.java;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import javax.lang.model.element.Modifier;
import java.io.IOException;
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
  public void writeClassBegin(Message message) throws IOException {
    core.writeClassBegin(message);
  }

  @Override
  public void writeClassEnd(Message message) throws IOException {
    core.writeClassEnd(message);
  }

  @Override
  public void writeField(Field field, String fieldTypeName, String fieldName, Set<Modifier> modifiers) throws IOException {
    core.writeField(field, fieldTypeName, fieldName, modifiers);
  }

  @Override
  public void writeGetterMethod(Field field, String fieldTypeName, String accessorName, String fieldName) throws IOException {
    core.writeGetterMethod(field, fieldTypeName, accessorName, fieldName);
  }

  @Override
  public void writeSetterMethod(Field field, String fieldTypeName, String accessorName, String fieldName) throws IOException {
    core.writeSetterMethod(field, fieldTypeName, accessorName, fieldName);
  }

  @Override
  public JavaWriter getOutput() {
    return core.getOutput();
  }

}
