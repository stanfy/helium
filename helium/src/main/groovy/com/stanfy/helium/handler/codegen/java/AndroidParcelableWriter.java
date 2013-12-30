package com.stanfy.helium.handler.codegen.java;

import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Writer for Android parcelables.
 */
public class AndroidParcelableWriter extends DelegateJavaClassWriter {

  private static final String ANDROID_OS_PARCEL = "android.os.Parcel";
  private static final String ANDROID_OS_PARCELABLE = "android.os.Parcelable";

  private final PojoGeneratorOptions options;

  public AndroidParcelableWriter(final JavaClassWriter core, final PojoGeneratorOptions options) {
    super(core);
    this.options = options;
  }

  @Override
  public void writeImports(final Set<String> imports) throws IOException {
    HashSet<String> newImports = new HashSet<String>(imports.size() + 3);
    newImports.addAll(imports);
    newImports.add(ANDROID_OS_PARCELABLE);
    newImports.add(ANDROID_OS_PARCEL);
    super.writeImports(newImports);
  }


  @Override
  public void writeClassBegin(final Message message, final String extending, final String... implementing) throws IOException {
    String[] newImplements = new String[implementing.length + 1];
    System.arraycopy(implementing, 0, newImplements, 0, implementing.length);
    newImplements[newImplements.length - 1] = ANDROID_OS_PARCELABLE;
    super.writeClassBegin(message, extending, newImplements);

    getOutput().emitEmptyLine();
    String className = message.getCanonicalName();
    String creatorBody = "{\n"
        + "    public " + className + " createFromParcel(Parcel source) {\n"
        + "      return new " + className + "(source);\n"
        + "    }\n"
        + "    public " + className + "[] newArray(int size) {\n"
        + "      return new " + className + "[size];\n"
        + "    }\n"
        + "  }";
    getOutput().emitField("Creator<" + className + ">", "CREATOR",
        new HashSet<Modifier>(Arrays.asList(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)),
        "new Creator<" + className + ">() " + creatorBody);
  }

  @Override
  public void writeConstructors(final Message message) throws IOException {
    getOutput().beginConstructor(Collections.singleton(Modifier.PUBLIC));
    getOutput().endConstructor();
    getOutput().emitEmptyLine();

    getOutput().beginConstructor(EnumSet.noneOf(Modifier.class), ANDROID_OS_PARCEL, "source");

    for (Field field : message.getFields()) {
      getOutput().emitStatement("this.%s = %s", options.getFieldName(field), getReadingStmt(field));
    }

    getOutput().endConstructor();
    getOutput().emitEmptyLine();

    super.writeConstructors(message);
  }

  @Override
  public void writeClassEnd(Message message) throws IOException {
    getOutput().emitEmptyLine();
    getOutput().emitAnnotation(Override.class);
    getOutput().beginMethod("int", "describeContents", EnumSet.of(Modifier.PUBLIC));
    getOutput().emitStatement("return 0");
    getOutput().endMethod();

    getOutput().emitEmptyLine();
    getOutput().emitAnnotation(Override.class);
    getOutput().beginMethod("void", "writeToParcel", EnumSet.of(Modifier.PUBLIC), ANDROID_OS_PARCEL, "dest", "int", "options");
    for (Field field : message.getFields()) {
      getOutput().emitStatement(getWritingStmt(field));
    }
    getOutput().endMethod();

    getOutput().emitEmptyLine();
    super.writeClassEnd(message);
  }

  private String getReadingStmt(final Field field) {
    String simpleMethod = getReadingMethod(field);
    if (simpleMethod != null) {
      return "source." + simpleMethod + "()";
    }
    throw new UnsupportedOperationException("cannot get reading statement for " + field);
  }

  private String getWritingStmt(final Field field) {
    String simpleMethod = getWritingMethod(field);
    if (simpleMethod != null) {
      return "dest." + simpleMethod + "(this." + options.getFieldName(field) + ")";
    }
    throw new UnsupportedOperationException("cannot get writing statement for " + field);
  }

  private String getReadingMethod(final Field field) {
    Class<?> clazz = JavaPrimitiveTypes.javaClass(field.getType());
    if (clazz == String.class) {
      return "readString";
    }
    if (clazz == int.class) {
      return "readInt";
    }
    if (clazz == long.class) {
      return "readLong";
    }
    if (clazz == float.class) {
      return "readFloat";
    }
    if (clazz == double.class) {
      return "readDouble";
    }
    return null;
  }

  private String getWritingMethod(final Field field) {
    Class<?> clazz = JavaPrimitiveTypes.javaClass(field.getType());
    if (clazz == String.class) {
      return "writeString";
    }
    if (clazz == int.class) {
      return "writeInt";
    }
    if (clazz == long.class) {
      return "writeLong";
    }
    if (clazz == float.class) {
      return "writeFloat";
    }
    if (clazz == double.class) {
      return "writeDouble";
    }
    return null;
  }

}
