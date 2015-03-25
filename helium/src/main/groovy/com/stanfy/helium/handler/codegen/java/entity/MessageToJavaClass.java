package com.stanfy.helium.handler.codegen.java.entity;

import com.stanfy.helium.handler.codegen.java.ClassAncestors;
import com.stanfy.helium.model.Descriptionable;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.constraints.ConstrainedType;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * Message to Java class converter.
 */
final class MessageToJavaClass {

  /** Writer. */
  private final JavaClassWriter writer;

  /** Generation options. */
  private final EntitiesGeneratorOptions options;

  public MessageToJavaClass(final JavaClassWriter writer, final EntitiesGeneratorOptions options) {
    this.writer = writer;
    this.options = options;
  }

  public void write(final Message message) throws IOException {
    String packageName = options.getPackageName();
    if (packageName == null) {
      throw new IllegalStateException("Package is not defined");
    }

    // package
    writer.getOutput().emitPackage(packageName);

    // imports
    HashSet<String> imports = new HashSet<String>();
    for (Field field : message.getActiveFields()) {

      Type type = field.getType();

      if (field.isSequence()) {
        String collectionName = options.getSequenceCollectionName();
        if (collectionName != null) {
          imports.add(collectionName);
        } else if (options.isAddToString()) {
          imports.add("java.util.Arrays");
        }
      }

      if (type.isPrimitive() && !(type instanceof ConstrainedType)) {
        Class<?> clazz = options.getJavaClass(type);
        if (!clazz.isPrimitive() && !"java.lang".equals(clazz.getPackage().getName())) {
          imports.add(clazz.getCanonicalName());
        }
      }

    }
    writer.writeImports(imports);

    // class name
    emitJavaDoc(message);

    final String extending;
    final String[] implementing;
    final Map<String, ClassAncestors> customParentMapping = options.getCustomParentMapping();
    ClassAncestors classAncestors = null;
    if (customParentMapping.containsKey(message.getName())) {
      final ClassAncestors externalParent = customParentMapping.get(message.getName());
      if (message.hasParent() && externalParent != null && externalParent.getExtending() != null) {
        throw new IllegalArgumentException(
                String.format("Bad configuration for message %s. It has a parent %s, and at the same time super class is configured for code generation %s",
                        message.getName(),
                        message.getParent().getName(),
                        externalParent.getExtending())
        );
      }
      classAncestors = externalParent;
    }

    final String messageParent = message.hasParent() ? message.getParent().getName() : null;
    if (classAncestors != null) {
      extending = classAncestors.getExtending() != null ? classAncestors.getExtending() : messageParent;
      implementing = classAncestors.getImplementing();
    } else {
      extending = messageParent;
      implementing = new String[]{};
    }

    writer.writeClassBegin(message, extending, implementing);
    writer.getOutput().emitEmptyLine();

    // fields
    for (Field field : message.getActiveFields()) {
      emitJavaDoc(field);
      writer.writeField(field, getFieldTypeName(field), options.getSafeFieldName(field), options.getFieldModifiers());
      writer.getOutput().emitEmptyLine();
    }
    writer.getOutput().emitEmptyLine();

    // constructors
    writer.writeConstructors(message);

    // access methods
    boolean getters = options.isAddGetters();
    boolean setters = options.isAddSetters();
    if (getters || setters) {
      for (Field field : message.getActiveFields()) {
        String fieldTypeName = getFieldTypeName(field);
        String fieldName = options.getSafeFieldName(field);
        if (getters) {
          writer.writeGetterMethod(field, fieldTypeName, getAccessMethodName("get", field), fieldName);
          writer.getOutput().emitEmptyLine();
        }
        if (setters) {
          writer.writeSetterMethod(field, fieldTypeName, getAccessMethodName("set", field), fieldName);
          writer.getOutput().emitEmptyLine();
        }
      }
    }

    // toString method
    if (options.isAddToString()) {
      generateToString(writer, message, false);
    }

    // end
    writer.writeClassEnd(message);
  }

  private void emitJavaDoc(final Descriptionable subject) throws IOException {
    if (subject.getDescription() != null) {
      String javadoc = subject.getDescription().trim();
      if (javadoc.length() == 0) {
        return;
      }
      if (!javadoc.endsWith(".")) {
        javadoc = javadoc.concat(".");
      }
      writer.getOutput().emitJavadoc("%s", javadoc);
    }
  }

  void generateToString(final JavaClassWriter writer,
                        final Message message,
                        final boolean singleLine) throws IOException {
    String indent = " ";
    String separator = ",";

    final Writer str = new Writer();
    if (message.getActiveFields().size() == 0) {
      str.add("return \"%s: has no fields\"", message.getName(), singleLine ? "" : "\n");
    } else {
      str.add("return \"%s: {%s", message.getName(), singleLine ? "\"\n" : "\\n\"\n");

      for (int i = 0; i < message.getActiveFields().size(); i++) {
        Field field = message.getActiveFields().get(i);
        String fieldName = options.getSafeFieldName(field);

        final String value;
        if (field.getType().isPrimitive() && !field.isSequence()) {
          value = fieldName;
        } else {
          String toString;

          if (options.getSequenceCollectionName() == null && field.isSequence()) {
            if (field.getType().isPrimitive()) {
              toString = "Arrays.toString(" + options.getName(field) + ")";
            } else {
              toString = "Arrays.deepToString(" + options.getName(field) + ")";
            }
          } else {
            toString = fieldName + ".toString()";
          }

          value = "(" + fieldName + " != null ? " + toString + " : \"null\")";
        }

        boolean notTheLastOne = i < message.getActiveFields().size() - 1;
        String ending = String.format(" + \"\\\"%s%s\"",
            notTheLastOne ? separator : "",
            singleLine ? " " : "\\n");

        str.add("%s+ \"%s%s=\\\"\" + %s%s\n",
            indent,
            singleLine ? "" : "  ",
            options.getSafeFieldName(field),
            value,
            ending);
      }

      str.add("%s+ \"}\"", indent, message.getName());
    }

    writer.getOutput().emitAnnotation(Override.class)
        .beginMethod("String", "toString", Collections.singleton(Modifier.PUBLIC))
        .emitStatement(str.toString())
        .endMethod();
  }

  private String getAccessMethodName(final String type, final Field field) {
    StringBuilder result = new StringBuilder().append(type).append(options.getName(field));
    result.setCharAt(type.length(), Character.toUpperCase(result.charAt(type.length())));
    return result.toString();
  }

  private String getFieldTypeName(final Field field) {
    return options.getJavaTypeName(field.getType(), field.getSequence(), writer.getOutput());
  }

  static class Writer extends StringWriter {
    public Writer add(final String pattern, final Object... args) {
      append(String.format(pattern, args));
      return this;
    }
  }

}
