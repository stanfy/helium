package com.stanfy.helium.handler.codegen.java.entity;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.handler.codegen.java.JavaPrimitiveTypes;
import com.stanfy.helium.internal.utils.Names;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.constraints.ConstrainedType;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Writer for Android parcelables.
 */
class AndroidParcelableWriter extends DelegateJavaClassWriter {

  /** Types supported by Parcel. */
  private static final Map<Class<?>, String> SUPPORTED_TYPES_BY_ANDROID = new HashMap<Class<?>, String>();
  static {
    SUPPORTED_TYPES_BY_ANDROID.put(String.class, String.class.getSimpleName());
    SUPPORTED_TYPES_BY_ANDROID.put(CharSequence.class, CharSequence.class.getSimpleName());
    SUPPORTED_TYPES_BY_ANDROID.put(int.class, "Int");
    SUPPORTED_TYPES_BY_ANDROID.put(long.class, "Long");
    SUPPORTED_TYPES_BY_ANDROID.put(float.class, "Float");
    SUPPORTED_TYPES_BY_ANDROID.put(double.class, "Double");
    SUPPORTED_TYPES_BY_ANDROID.put(byte.class, "Byte");
    SUPPORTED_TYPES_BY_ANDROID.put(short.class, "Short");
  }

  private static final String ANDROID_OS_PARCEL = "android.os.Parcel";
  private static final String ANDROID_OS_PARCELABLE = "android.os.Parcelable";

  private final EntitiesGeneratorOptions options;

  public AndroidParcelableWriter(final JavaClassWriter core, final EntitiesGeneratorOptions options) {
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
        + "  public " + className + " createFromParcel(Parcel source) {\n"
        + "    return new " + className + "(source);\n"
        + "  }\n"
        + "  public " + className + "[] newArray(int size) {\n"
        + "    return new " + className + "[size];\n"
        + "  }\n"
        + "}";
    getOutput().emitField("Creator<" + className + ">", "CREATOR",
        new HashSet<Modifier>(Arrays.asList(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)),
        "new Creator<" + className + ">() " + creatorBody);
  }

  @Override
  public void writeConstructors(final Message message) throws IOException {
    JavaWriter output = getOutput();
    output.beginConstructor(Collections.singleton(Modifier.PUBLIC));
    output.endConstructor();
    output.emitEmptyLine();

    output.beginConstructor(EnumSet.noneOf(Modifier.class), ANDROID_OS_PARCEL, "source");
    if (message.hasParent()) {
      output.emitStatement("super(source)");
    }

    for (Field field : message.getActiveFields()) {
      emitReadingStmt(field);
    }

    output.endConstructor();
    output.emitEmptyLine();

    super.writeConstructors(message);
  }

  @Override
  public void writeClassEnd(final Message message) throws IOException {
    JavaWriter output = getOutput();
    output.emitEmptyLine();
    output.emitAnnotation(Override.class);
    output.beginMethod("int", "describeContents", EnumSet.of(Modifier.PUBLIC));
    output.emitStatement("return 0");
    output.endMethod();

    output.emitEmptyLine();
    output.emitAnnotation(Override.class);
    output.beginMethod("void", "writeToParcel", EnumSet.of(Modifier.PUBLIC), ANDROID_OS_PARCEL, "dest", "int", "options");

    // We assume that if parent is specified, it's also a parcelable.
    if (message.hasParent()) {
      output.emitStatement("super.writeToParcel(dest, options)");
    }

    for (Field field : message.getActiveFields()) {
      emitWritingStmt(field);
    }
    output.endMethod();

    output.emitEmptyLine();
    super.writeClassEnd(message);
  }

  private void emitReadingStmt(final Field field) throws IOException {
    String fieldName = options.getSafeFieldName(field);
    JavaWriter output = getOutput();

    String simpleMethod = getSupportedMethod("read", field);
    if (simpleMethod != null) {
      if (field.isSequence()) {
        simpleMethod = simpleMethod.concat("Array").replace("read", "create");
      }
      output.emitStatement("this.%1$s = source.%2$s()", fieldName, simpleMethod);
      return;
    }

    String classLoader = "getClass().getClassLoader()";

    Class<?> clazz = getJavaClass(field);
    if (clazz != null) {
      // boolean?
      if (clazz == boolean.class) {
        readBoolean(field, fieldName, output);
        return;
      }
    }

    if (field.getType() instanceof Message) {
      // read Parcelable
      readParcelable(field, fieldName, output, field.getType().getCanonicalName(), classLoader);
      return;
    }

    if (options.isEnumDeclaration(field.getType())) {
      readEnumFromParcelable(fieldName, output, Names.capitalize(field.getType().getCanonicalName()), field.isSequence());
      return;
    }

    String className = clazz != null ? clazz.getCanonicalName() : options.getPrimitiveTypeName(field.getType());

    // date?
    if ("java.util.Date".equals(className)) {
      output.emitStatement("long %1$sValue = source.readLong()", fieldName);
      output.emitStatement("this.%1$s = %1$sValue != -1 ? new Date(%1$sValue) : null", fieldName);
      return;
    }

    String resultClassName = output.compressType(className);
    if (field.isSequence()) {
      readParcelable(field, fieldName, output, resultClassName, classLoader);
    } else {
      output.emitStatement("this.%1$s = (%2$s) source.readValue(%3$s)",
          fieldName,
          resultClassName,
          classLoader);
    }
  }

  private static void readEnumFromParcelable(String fieldName, JavaWriter output, String name, boolean sequence) throws IOException {
    String enumName = output.compressType(name);

    if (!sequence) {
      output.emitStatement("int %1$sOrdinal = source.readInt()", fieldName);
      output.emitStatement("this.%1$s = %1$sOrdinal != -1 ? %2$s.values()[%1$sOrdinal] : null", fieldName, enumName);
      return;
    }

    output.emitStatement("int[] %1$sOrdinals = source.createIntArray()", fieldName);
    output.beginControlFlow("if (%1$sOrdinals != null)", fieldName);
    output.emitStatement("this.%1$s = new %2$s[%1$sOrdinals.length]", fieldName, enumName);
    output.beginControlFlow("for (int i = 0; i < %1$sOrdinals.length; i++)", fieldName);
    output.emitStatement("this.%1$s[i] = %1$sOrdinals[i] != -1 ? %2$s.values()[%1$sOrdinals[i]] : null", fieldName, enumName);
    output.endControlFlow();
    output.endControlFlow();
  }

  private static void readBoolean(final Field field, final String fieldName, final JavaWriter output) throws IOException {
    if (field.isSequence()) {
      output.emitStatement("int %1$sCount = source.readInt()", fieldName);
      output.beginControlFlow("if (" + fieldName + "Count > 0)");
      output.emitStatement("this.%1$s = new boolean[%1$sCount]", fieldName);
      output.beginControlFlow("for (int i = 0; i < " + fieldName + "Count; i++)");
      output.emitStatement("this.%1$s[i] = source.readInt() == 1", fieldName);
      output.endControlFlow();
      output.endControlFlow();
    } else {
      output.emitStatement("this.%1$s = source.readInt() == 1", fieldName);
    }
  }

  private static void readParcelable(final Field field, final String fieldName, final JavaWriter output,
                                     final String className, final String classLoader) throws IOException {
    String shortClassName = output.compressType(className);
    if (field.isSequence()) {
      output.emitStatement("Parcelable[] %1$sParcelables = source.readParcelableArray(%2$s)",
          fieldName,
          classLoader);
      output.beginControlFlow("if (" + fieldName + "Parcelables != null)");
      output.emitStatement("this.%1$s = new %2$s[%1$sParcelables.length]", fieldName, JavaWriter.rawType(shortClassName));
      output.beginControlFlow("for (int i = 0; i < " + fieldName + "Parcelables.length; i++)");
      output.emitStatement("this.%1$s[i] = (%2$s) %1$sParcelables[i]", fieldName, shortClassName);
      output.endControlFlow();
      output.endControlFlow();
    } else {
      output.emitStatement("this.%1$s = (%2$s) source.readParcelable(%3$s)",
          fieldName,
          shortClassName,
          classLoader);
    }
  }

  private void emitWritingStmt(final Field field) throws IOException {
    String simpleMethod = getSupportedMethod("write", field);
    JavaWriter output = getOutput();
    String fieldName = options.getSafeFieldName(field);

    if (simpleMethod != null) {
      if (field.isSequence()) {
        simpleMethod = simpleMethod.concat("Array");
      }
      output.emitStatement("dest.%s(this.%s)", simpleMethod, fieldName);
      return;
    }

    Class<?> clazz = getJavaClass(field);
    if (clazz != null) {
      // boolean?
      if (clazz == boolean.class) {
        writeBoolean(field, output, fieldName);
        return;
      }
    }

    if (field.getType() instanceof Message) {
      // turn it into Parcelable
      writeParcelable(output, field, fieldName);
      return;
    }

    // enum?
    if (options.isEnumDeclaration(field.getType())) {
      writeEnumToParcel(output, fieldName, field.isSequence());
      return;
    }

    String className = clazz != null ? clazz.getCanonicalName() : options.getPrimitiveTypeName(field.getType());

    // date?
    if ("java.util.Date".equals(className)) {
      output.emitStatement("dest.writeLong(this.%1$s != null ? this.%1$s.getTime() : -1L)", fieldName);
      return;
    }

    if (field.isSequence()) {
      output.emitStatement("dest.writeParcelableArray(this.%s, options)", fieldName);
    } else {
      output.emitStatement("dest.writeValue(this.%s)", fieldName);
    }
  }

  private static void writeEnumToParcel(JavaWriter output, String fieldName, boolean sequence) throws IOException {
    if (!sequence) {
      output.emitStatement("dest.writeInt(this.%1$s != null ? this.%1$s.ordinal() : -1)", fieldName);
      return;
    }

    output.emitStatement("int[] %1$sOrdinals = null", fieldName);
    output.beginControlFlow("if (this.%1$s != null)", fieldName);
    output.emitStatement("%1$sOrdinals = new int[this.%1$s.length]", fieldName);
    output.beginControlFlow("for (int i = 0; i < %1$sOrdinals.length; i++)", fieldName);
    output.emitStatement("%1$sOrdinals[i] = this.%1$s[i] != null ? this.%1$s[i].ordinal() : -1", fieldName);
    output.endControlFlow();
    output.endControlFlow();
    output.emitStatement("dest.writeIntArray(%1$sOrdinals)", fieldName);

  }

  private static boolean isAndroidParcelable(final Class<?> clazz) {
    for (Class<?> intf : clazz.getInterfaces()) {
      if ("android.os.Parcelable".equals(intf.getCanonicalName())) {
        return true;
      }
    }
    return false;
  }

  private static void writeParcelable(final JavaWriter output, final Field field, final String fieldName)
      throws IOException {
    output.emitStatement("dest.writeParcelable%1$s(this.%2$s, options)",
        field.isSequence() ? "Array" : "",
        fieldName);
  }

  private static void writeBoolean(final Field field, final JavaWriter output, final String fieldName)
      throws IOException {
    if (field.isSequence()) {
      output.emitStatement("int %1$sCount = this.%1$s != null ? this.%1$s.length : 0", fieldName);
      output.emitStatement("dest.writeInt(%1$sCount)", fieldName);
      output.beginControlFlow("for (int i = 0; i < " + fieldName + "Count; i++)");
      output.emitStatement("dest.writeInt(this.%1$s[i] ? 1 : 0)", fieldName);
      output.endControlFlow();
    }  else {
      output.emitStatement("dest.writeInt(this.%1$s ? 1 : 0)", fieldName);
    }
  }

  private Class<?> getJavaClass(final Field field) {
    Class<?> clazz = field.getType().isPrimitive() && !(field.getType() instanceof ConstrainedType)
        ? options.getPrimitiveJavaClass(field.getType())
        : null;
    if (clazz != null && !field.isRequired() && options.isBoxPrimitiveOptionals()) {
      clazz = JavaPrimitiveTypes.box(clazz);
    }
    return clazz;
  }

  private String getSupportedMethod(final String prefix, final Field field) {
    Class<?> clazz = getJavaClass(field);
    String namePart = SUPPORTED_TYPES_BY_ANDROID.get(clazz);
    return namePart != null ? prefix.concat(namePart) : null;
  }

}
