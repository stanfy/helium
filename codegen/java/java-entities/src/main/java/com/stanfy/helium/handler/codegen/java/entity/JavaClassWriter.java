package com.stanfy.helium.handler.codegen.java.entity;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Set;

/**
 * Writes Java class code.
 */
public interface JavaClassWriter {

  void writeImports(final Set<String> imports) throws IOException;

  void writeClassBegin(final Message message, final String extending, final String... implementing) throws IOException;

  void writeClassEnd(final Message message) throws IOException;

  void writeField(final Field field, final String fieldTypeName, final String fieldName, final Set<Modifier> modifiers) throws IOException;

  void writeSetterMethod(final Field field, final String fieldTypeName, final String accessorName, final String fieldName) throws IOException;

  void writeGetterMethod(final Field field, final String fieldTypeName, final String accessorName, final String fieldName) throws IOException;

  JavaWriter getOutput();

  void writeConstructors(final Message message) throws IOException;

  void writeEnumValue(final String name, final boolean isLast) throws IOException;

}
