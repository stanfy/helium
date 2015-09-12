package com.stanfy.helium.handler.codegen.java.entity;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.constraints.EnumConstraint;
import com.stanfy.helium.internal.utils.Names;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Generates Java enum from constraints.
 */
final class ConstraintsToEnum {

  private final EntitiesGeneratorOptions options;

  ConstraintsToEnum(final EntitiesGeneratorOptions options) {
    this.options = options;
  }

  public void write(final Type type, final EnumConstraint<String> constraint, final Writer output) throws IOException {
    JavaWriter jw = new JavaWriter(output);
    jw.emitPackage(options.getPackageName());

    if (type.getDescription() != null) {
      jw.emitJavadoc(type.getDescription());
    }

    String enumName = Names.capitalize(type.getCanonicalName());
    jw.beginType(enumName, "enum", EnumSet.of(PUBLIC));
    ArrayList<String> enumValues = new ArrayList<String>(constraint.getValues().size());
    for (String value: constraint.getValues()) {
      enumValues.add(Names.canonicalName(value).toUpperCase(Locale.US));
    }
    jw.emitEnumValues(enumValues);

    jw.endType();
  }

}
