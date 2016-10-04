package com.stanfy.helium.handler.codegen.java;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.DefaultType;
import com.stanfy.helium.handler.codegen.GeneratorOptions;
import com.stanfy.helium.model.Descriptionable;
import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.constraints.ConstrainedType;
import com.stanfy.helium.model.constraints.EnumConstraint;
import com.stanfy.helium.internal.utils.Names;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Java generator options.
 */
public abstract class JavaGeneratorOptions extends GeneratorOptions {

  /** Reserved java keywords. */
  private static final Set<String> JAVA_KEYWORDS = new HashSet<String>(Arrays.asList(
      "abstract", "continue", "for", "new", "switch", "assert", "default", "goto",
      "package", "synchronized", "boolean", "do", "if", "private", "this", "break",
      "double", "implements", "protected", "throw", "byte", "else", "import", "public",
      "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends",
      "int", "short", "try", "char", "final", "interface", "static", "void", "class",
      "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while"
  ));

  /** Collection class name for sequences. */
  private String sequenceCollectionName = List.class.getCanonicalName();

  /** Mapping for custom primitives. */
  private Map<String, String> customPrimitivesMapping = Collections.emptyMap();

  /**
   * Set of custom mapping for messages. Used when you need classes
   * generated from messages extend custom classes and implement custom
   * interfaces.
   *
   * @see com.stanfy.helium.model.Message#parent
   */
  private Map<String, ClassAncestors> customParentMapping = Collections.emptyMap();

  /** Whether to prettify field names. */
  private boolean prettifyNames;

  /** Package name for generated classes. */
  private String packageName;

  public String getSequenceCollectionName() {
    return sequenceCollectionName;
  }

  public void setSequenceCollectionName(final String sequenceCollectionName) {
    this.sequenceCollectionName = sequenceCollectionName;
  }

  public void useArraysForSequences() {
    setSequenceCollectionName(null);
  }

  public Map<String, String> getCustomPrimitivesMapping() {
    return customPrimitivesMapping;
  }

  public void setCustomPrimitivesMapping(final Map<String, String> customPrimitivesMapping) {
    this.customPrimitivesMapping = customPrimitivesMapping;
  }

  public Map<String, ClassAncestors> getCustomParentMapping() {
    return customParentMapping;
  }

  public void setCustomParentMapping(final Map<String, ClassAncestors> customParentMapping) {
    this.customParentMapping = customParentMapping;
  }

  public String getJavaTypeName(final Type type, final boolean sequence, final JavaWriter writer) {
    final String typeName;
    if (type instanceof Message) {
      if (sequence) {
        typeName = writer.compressType(getSequenceTypeName(type.getCanonicalName()));
      } else {
        typeName = type.getCanonicalName();
      }
    } else if (type instanceof Dictionary) {
      Dictionary dict = (Dictionary) type;
      return javaMap(dict, writer);
    } else if (type.isPrimitive()) {

      if (isEnumDeclaration(type)) {
        String enumName = Names.capitalize(type.getCanonicalName());
        if (!sequence) {
          return enumName;
        }
        return writer.compressType(getSequenceTypeName(enumName));
      }

      if (getCustomPrimitivesMapping().containsKey(type.getName())) {
        String customName = getCustomPrimitivesMapping().get(type.getName());
        typeName = writer.compressType(sequence ? getSequenceTypeName(customName) : customName);
      } else {
        Class<?> clazz = getJavaClass(type);
        if (sequence) {
          String itemClassName = getSequenceItemClassName(clazz);
          typeName = writer.compressType(getSequenceTypeName(itemClassName));
        } else {
          typeName = writer.compressType(clazz.getCanonicalName());
        }
      }

    } else {
      throw new UnsupportedOperationException("Cannot resolve Java type for " + type + ", sequence: " + sequence);
    }
    return typeName;
  }

  private String javaMap(final Dictionary dict, final JavaWriter writer) {
    return "Map<" + mapMemberType(dict.getKey(), writer) + ", " + mapMemberType(dict.getValue(), writer) + ">";
  }

  private String mapMemberType(Type type, JavaWriter writer) {
    final String name;
    if (type.isPrimitive()) {
      Class<?> keyClass = getJavaClass(type);
      name = writer.compressType(JavaPrimitiveTypes.box(keyClass).getName());
    } else if (type instanceof Sequence) {
      name = getJavaTypeName(((Sequence) type).getItemsType(), true, writer);
    } else {
      name = getJavaTypeName(type, false, writer);
    }
    return name;
  }

  public boolean isEnumDeclaration(final Type type) {
    if (!(type instanceof ConstrainedType)) {
      return false;
    }
    ConstrainedType cType = (ConstrainedType) type;
    return cType.getBaseType().getName().equals(DefaultType.STRING.getLangName())
        && cType.containsConstraint(EnumConstraint.class);
  }

  public Class<?> getJavaClass(final Type type) {
    Class<?> result = JavaPrimitiveTypes.javaClass(type);
    if (result == null) {
      String className = getCustomPrimitivesMapping().get(type.getName());
      if (className == null) {
        throw new IllegalStateException("Mapping for " + type + " is not defined");
      }
      try {
        result = Thread.currentThread().getContextClassLoader().loadClass(className);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  public String getSequenceTypeName(final String itemJavaClass) {
    String collectionName = getSequenceCollectionName();
    if (collectionName != null) {
      return collectionName + "<" + itemJavaClass + ">";
    }
    return itemJavaClass + "[]";
  }

  public String getSequenceItemClassName(final Class<?> javaType) {
    return getSequenceCollectionName() != null
        ? JavaPrimitiveTypes.box(javaType).getCanonicalName()
        : javaType.getCanonicalName();
  }

  public void setPrettifyNames(final boolean prettifyNames) {
    this.prettifyNames = prettifyNames;
  }

  public boolean isPrettifyNames() {
    return prettifyNames;
  }

  public String getName(final Descriptionable d) {
    String name = d.getCanonicalName();
    return isPrettifyNames() ? Names.prettifiedName(name) : name;
  }

  public String getSafeFieldName(final Field d) {
    final String fieldName = getName(d);
    if (JAVA_KEYWORDS.contains(fieldName)) {
      return fieldName.concat("Field");
    }
    return fieldName;
  }

  public String getSafeParameterName(final String name) {
    return JAVA_KEYWORDS.contains(name) ? name.concat("Param") : name;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(final String packageName) {
    if (packageName == null) {
      throw new IllegalArgumentException("Package name cannot be null");
    }
    this.packageName = packageName;
  }

}
