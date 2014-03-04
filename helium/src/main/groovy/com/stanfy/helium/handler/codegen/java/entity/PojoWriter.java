package com.stanfy.helium.handler.codegen.java.entity;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.handler.codegen.java.entity.JavaClassWriter;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Writes type as a Java class.
 */
public class PojoWriter implements JavaClassWriter {

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
    output.emitField(fieldTypeName, safeFieldName(fieldName), modifiers);
  }

  @Override
  public void writeSetterMethod(final Field field, final String fieldTypeName, final String accessorName, final String fieldName) throws IOException {
    output.beginMethod("void", accessorName, Collections.singleton(Modifier.PUBLIC), fieldTypeName, "value");
    output.emitStatement("this.%s = value", safeFieldName(fieldName));
    output.endMethod();
  }

  @Override
  public void writeGetterMethod(final Field field, final String fieldTypeName, final String accessorName, final String fieldName) throws IOException {
    output.beginMethod(fieldTypeName, accessorName, Collections.singleton(Modifier.PUBLIC));
    output.emitStatement("return this.%s", safeFieldName(fieldName));
    output.endMethod();
  }

  @Override
  public void writeClassEnd(Message message) throws IOException {
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

  private static String safeFieldName(final String fieldName) {
    if (JAVA_KEYWORDS.contains(fieldName)) {
      return fieldName.concat("Field");
    }
    return fieldName;
  }

  /** Reserved java keywords. */
  private static final Set<String> JAVA_KEYWORDS = new HashSet<String>(Arrays.asList(
    "abstract",	"continue",	"for", "new",	"switch",
    "assert",  "default",  "goto",  "package",  "synchronized",
    "boolean",  "do",  "if",  "private",  "this",
    "break",  "double",  "implements",  "protected",  "throw",
    "byte",  "else",  "import",  "public",  "throws",
    "case",  "enum",  "instanceof",  "return",  "transient",
    "catch",  "extends",  "int",  "short",  "try",
    "char",  "final",  "interface",  "static",  "void",
    "class",  "finally",  "long",  "strictfp",  "volatile",
    "const",  "float",  "native",  "super",  "while"
  ));

}
