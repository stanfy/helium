package com.stanfy.helium.handler.codegen.java;

import com.google.gson.annotations.SerializedName;
import com.stanfy.helium.model.Field;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Adds {@code SerializedName} annotations.
 */
public class GsonPojoWriter extends DelegateJavaClassWriter {

  public GsonPojoWriter(JavaClassWriter core) {
    super(core);
  }

  @Override
  public void writeImports(final Set<String> imports) throws IOException {
    HashSet<String> newImports = new HashSet<String>(imports.size() + 1);
    newImports.add(SerializedName.class.getCanonicalName());
    super.writeImports(newImports);
  }

  @Override
  public void writeField(final Field field, final String fieldTypeName, final String fieldName, final Set<Modifier> modifiers) throws IOException {
    getOutput().emitAnnotation(SerializedName.class, "\"" + field.getName() + "\"");
    super.writeField(field, fieldTypeName, fieldName, modifiers);
  }

}
