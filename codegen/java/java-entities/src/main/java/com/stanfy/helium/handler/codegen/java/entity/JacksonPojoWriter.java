package com.stanfy.helium.handler.codegen.java.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stanfy.helium.model.Field;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Adds {@code JsonProperty} annotations.
 */
class JacksonPojoWriter extends DelegateJavaClassWriter {

  public JacksonPojoWriter(final JavaClassWriter core) {
    super(core);
  }

  @Override
  public void writeImports(final Set<String> imports) throws IOException {
    HashSet<String> newImports = new HashSet<String>(imports.size() + 1);
    newImports.addAll(imports);
    newImports.add(JsonProperty.class.getCanonicalName());
    super.writeImports(newImports);
  }

  @Override
  public void writeField(final Field field, final String fieldTypeName, final String fieldName, final Set<Modifier> modifiers) throws IOException {
    getOutput().emitAnnotation(JsonProperty.class, "\"" + field.getName() + "\"");
    super.writeField(field, fieldTypeName, fieldName, modifiers);
  }

}
