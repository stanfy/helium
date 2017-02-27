package com.stanfy.helium.handler.codegen.java.entity;

import com.google.gson.annotations.SerializedName;
import com.stanfy.helium.model.Field;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Adds {@code SerializedName} annotations.
 */
class GsonPojoWriter extends DelegateJavaClassWriter {

  public GsonPojoWriter(final JavaClassWriter core) {
    super(core);
  }

  @Override
  public void writeImports(final Set<String> imports) throws IOException {
    HashSet<String> newImports = new HashSet<String>(imports.size() + 1);
    newImports.addAll(imports);
    newImports.add(SerializedName.class.getCanonicalName());
    super.writeImports(newImports);
  }

  @Override
  public void writeField(final Field field, final String fieldTypeName, final String fieldName, final Set<Modifier> modifiers) throws IOException {
    if (field.getAlternatives() != null && field.getAlternatives().size() > 0) {
      Map<String, Object> values = new HashMap<>();
      values.put("value", "\"" + field.getName() + "\"");
      String[] alternates = new String[field.getAlternatives().size()];
      for (int i = 0; i < field.getAlternatives().size(); i++) {
        alternates[i] = "\"" + field.getAlternatives().get(i) + "\"";
      }
      values.put("alternate", alternates);
      getOutput().emitAnnotation(SerializedName.class, values);
    } else {
      getOutput().emitAnnotation(SerializedName.class, "\"" + field.getName() + "\"");
    }
    super.writeField(field, fieldTypeName, fieldName, modifiers);
  }

  @Override
  public void writeEnumValue(String name, boolean isLast) throws IOException {
    getOutput().emitAnnotation(SerializedName.class, "\"" + name + "\"");
    super.writeEnumValue(name, isLast);
  }
}
