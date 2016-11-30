package com.stanfy.helium.handler.codegen.java.entity;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.internal.utils.Names;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.constraints.EnumConstraint;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Generates Java enum from constraints.
 */
final class ConstraintsToEnum {

  private final JavaClassWriter javaClassWriter;
  private final EntitiesGeneratorOptions options;

  ConstraintsToEnum(final JavaClassWriter javaClassWriter, final EntitiesGeneratorOptions options) {
    this.javaClassWriter = javaClassWriter;
    this.options = options;
  }

  public void write(final Type type, final EnumConstraint<String> constraint) throws IOException {
    JavaWriter jw = javaClassWriter.getOutput();
    jw.emitPackage(options.getPackageName());
    javaClassWriter.writeImports(Collections.<String>emptySet());

    if (type.getDescription() != null) {
      jw.emitJavadoc(type.getDescription());
    }

    String enumName = Names.capitalize(type.getCanonicalName());
    jw.beginType(enumName, "enum", EnumSet.of(PUBLIC));

    Iterator<String> valuesIterator = constraint.getValues().iterator();
    while (valuesIterator.hasNext()) {
      String value = valuesIterator.next();

      javaClassWriter.writeEnumValue(value, !valuesIterator.hasNext());
    }

    jw.endType();
  }

}
