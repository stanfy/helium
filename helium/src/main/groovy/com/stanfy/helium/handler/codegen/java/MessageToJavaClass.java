package com.stanfy.helium.handler.codegen.java;

import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.HashSet;

/**
 * Message to Java class converter.
 */
public class MessageToJavaClass {

  /** Writer. */
  private final JavaClassWriter writer;

  /** Generation options. */
  private final PojoGeneratorOptions options;

  public MessageToJavaClass(final JavaClassWriter writer, final PojoGeneratorOptions options) {
    this.writer = writer;
    this.options = options;
  }

  public void write(final Message message) throws IOException {
    String collectionName = options.getSequenceCollectionName();
    if (collectionName == null) {
      throw new IllegalStateException("collection name for sequences is not defined");
    }
    String packageName = options.getPackageName();
    if (packageName == null) {
      throw new IllegalStateException("Package is not defined");
    }

    // package
    writer.getOutput().emitPackage(packageName);

    // imports
    HashSet<String> imports = new HashSet<String>();
    for (Field field : message.getFields()) {
      Type type = field.getType();

      if (field.isSequence()) {
        imports.add(collectionName);
      }

      if (type.isPrimitive()) {
        Class<?> clazz = options.getJavaClass(type);
        if (!clazz.isPrimitive() && !"java.lang".equals(clazz.getPackage().getName())) {
          imports.add(clazz.getCanonicalName());
        }
      }

    }
    writer.writeImports(imports);

    // class name
    writer.writeClassBegin(message, null);
    writer.getOutput().emitEmptyLine();

    // fields
    for (Field field : message.getFields()) {
      writer.writeField(field, getFieldTypeName(field), options.getFieldName(field), options.getFieldModifiers());
      writer.getOutput().emitEmptyLine();
    }
    writer.getOutput().emitEmptyLine();

    // constructors
    writer.writeConstructors(message);

    // access methods
    boolean getters = options.isAddGetters();
    boolean setters = options.isAddSetters();
    if (getters || setters) {
      for (Field field : message.getFields()) {
        String fieldTypeName = getFieldTypeName(field);
        String fieldName = options.getFieldName(field);
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

    // end
    writer.writeClassEnd(message);
  }

  private String getAccessMethodName(final String type, final Field field) {
    StringBuilder result = new StringBuilder().append(type).append(options.getFieldName(field));
    result.setCharAt(type.length(), Character.toUpperCase(result.charAt(type.length())));
    return result.toString();
  }

  private String getFieldTypeName(final Field field) {
    String collectionName = options.getSequenceCollectionName();
    Type type = field.getType();
    final String typeName;
    if (type instanceof Message) {
      if (field.isSequence()) {
        typeName = writer.getOutput().compressType(collectionName + "<" + type.getCanonicalName() + ">");
      } else {
        typeName = type.getCanonicalName();
      }
    } else if (type.isPrimitive()) {
      Class<?> clazz = options.getJavaClass(type);
      if (field.isSequence()) {
        typeName = writer.getOutput().compressType(collectionName + "<" + JavaPrimitiveTypes.box(clazz).getCanonicalName() + ">");
      } else {
        typeName = writer.getOutput().compressType(clazz.getCanonicalName());
      }
    } else {
      throw new UnsupportedOperationException("Cannot write field " + field);
    }
    return typeName;
  }

}