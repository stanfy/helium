package com.stanfy.helium.handler.codegen.java;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes type as a Java class.
 */
class TypeToJavaClass {

  /** Package name. */
  private final String packageName;

  /** Output. */
  private final JavaWriter output;

  public TypeToJavaClass(final String packageName, final Writer output) {
    this.packageName = packageName;
    this.output = new JavaWriter(output);
  }

  public void write(final Type type) throws IOException {
    output.emitPackage(packageName);
    output.beginType(type.getName(), "class");
  }

}
