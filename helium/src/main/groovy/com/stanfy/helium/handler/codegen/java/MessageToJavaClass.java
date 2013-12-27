package com.stanfy.helium.handler.codegen.java;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.utils.Names;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Writes type as a Java class.
 */
public class MessageToJavaClass {

  /** Output. */
  private final JavaWriter output;

  /** Options. */
  private final PojoGeneratorOptions options;

  public MessageToJavaClass(final Writer output, final PojoGeneratorOptions options) {
    this.output = new JavaWriter(output);
    this.options = options;
  }


  protected void writeImports(final Set<String> imports) throws IOException {
    if (!imports.isEmpty()) {
      output.emitImports(imports);
      output.emitEmptyLine();
    }
  }

  protected void writeClassBegin(final Message message) throws IOException {
    output.beginType(message.getCanonicalName(), "class", Collections.singleton(Modifier.PUBLIC));
  }

  protected void writeField(final Field field) throws IOException {
    output.emitField(getFieldTypeName(field), getFieldName(field), options.getFieldModifiers());
  }

  protected void writeSetterMethod(final Field field) throws IOException {
    output.beginMethod("void", getAccessMethodName("set", field), Collections.singleton(Modifier.PUBLIC), getFieldTypeName(field), "value");
    output.emitStatement("%s = value", getFieldName(field));
    output.endMethod();
  }

  protected void writeGetterMethod(final Field field) throws IOException {
    output.beginMethod(getFieldTypeName(field), getAccessMethodName("get", field), Collections.singleton(Modifier.PUBLIC));
    output.emitStatement("return %s", getFieldName(field));
    output.endMethod();
  }

  protected JavaWriter getOutput() {
    return output;
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
    output.emitPackage(packageName);

    // imports
    HashSet<String> imports = new HashSet<String>();
    for (Field field : message.getFields()) {
      Type type = field.getType();

      if (field.isSequence()) {
        imports.add(collectionName);
      }

      if (type.isPrimitive()) {
        Class<?> clazz = getJavaClass(type);
        if (!clazz.isPrimitive() && !"java.lang".equals(clazz.getPackage().getName())) {
          imports.add(clazz.getCanonicalName());
        }
      }

    }
    writeImports(imports);

    // class name
    writeClassBegin(message);
    output.emitEmptyLine();

    // fields
    for (Field field : message.getFields()) {
      writeField(field);
      output.emitEmptyLine();
    }
    output.emitEmptyLine();

    // access methods
    boolean getters = options.isAddGetters();
    boolean setters = options.isAddSetters();
    if (getters || setters) {
      for (Field field : message.getFields()) {
        if (getters) {
          writeGetterMethod(field);
          output.emitEmptyLine();
        }
        if (setters) {
          writeSetterMethod(field);
          output.emitEmptyLine();
        }
      }
    }

    // end
    output.endType();
  }

  private Class<?> getJavaClass(final Type type) {
    Class<?> result = JavaPrimitiveTypes.javaClass(type);
    if (result == null) {
      String className = options.getCustomPrimitivesMapping().get(type.getName());
      if (className == null) {
        throw new IllegalStateException("Mapping for " + type + " is not defined");
      }
      try {
        result = Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  private String getAccessMethodName(final String type, final Field field) {
    StringBuilder result = new StringBuilder().append(type).append(getFieldName(field));
    result.setCharAt(type.length(), Character.toUpperCase(result.charAt(type.length())));
    return result.toString();
  }

  private String getFieldTypeName(final Field field) {
    String collectionName = options.getSequenceCollectionName();
    Type type = field.getType();
    final String typeName;
    if (type instanceof Message) {
      if (field.isSequence()) {
        typeName = output.compressType(collectionName + "<" + type.getCanonicalName() + ">");
      } else {
        typeName = type.getCanonicalName();
      }
    } else if (type.isPrimitive()) {
      Class<?> clazz = getJavaClass(type);
      if (field.isSequence()) {
        typeName = output.compressType(collectionName + "<" + JavaPrimitiveTypes.box(clazz).getCanonicalName() + ">");
      } else {
        typeName = output.compressType(clazz.getCanonicalName());
      }
    } else {
      throw new UnsupportedOperationException("Cannot write field " + field);
    }
    return typeName;
  }

  private String getFieldName(final Field field) {
    String name = field.getCanonicalName();
    return options.isPrettifyNames() ? Names.prettifiedName(name) : name;
  }

}
