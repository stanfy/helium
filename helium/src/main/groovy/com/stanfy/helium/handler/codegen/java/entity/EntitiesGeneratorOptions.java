package com.stanfy.helium.handler.codegen.java.entity;

import com.stanfy.helium.handler.codegen.java.JavaGeneratorOptions;
import com.stanfy.helium.handler.codegen.java.JavaPrimitiveTypes;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.utils.Names;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.stanfy.helium.handler.codegen.java.entity.Writers.WriterWrapper;

/**
 * Options for POJO generator.
 */
public class EntitiesGeneratorOptions extends JavaGeneratorOptions {

  private static final long serialVersionUID = 1;

  /** Default options. */
  public static EntitiesGeneratorOptions defaultOptions(final String packageName) {
    EntitiesGeneratorOptions options = new EntitiesGeneratorOptions();
    options.setFieldModifiers(new HashSet<Modifier>(Arrays.asList(Modifier.PUBLIC)));
    options.setPackageName(packageName);
    return options;
  }


  /** Field modifiers. */
  private Set<Modifier> fieldModifiers = Collections.emptySet();

  /** Whether to generate setters. */
  private boolean addSetters;

  /** Whether to generate getters. */
  private boolean addGetters;

  /** Collection class name for sequences. */
  private String sequenceCollectionName = List.class.getCanonicalName();

  /** Mapping for custom primitives. */
  private Map<String, String> customPrimitivesMapping = Collections.emptyMap();

  /** Writer  */
  private WriterWrapper writerWrapper = Writers.chain();

  /** Whether to prettify field names. */
  private boolean prettifyNames;

  public Set<Modifier> getFieldModifiers() {
    return fieldModifiers;
  }

  public void setFieldModifiers(final Set<Modifier> fieldModifiers) {
    if (fieldModifiers == null) {
      throw new IllegalArgumentException("Field modifiers cannot be null. Provide empty set instead.");
    }
    this.fieldModifiers = fieldModifiers;
  }

  public boolean isAddSetters() {
    return addSetters;
  }

  public void setAddSetters(final boolean addSetters) {
    this.addSetters = addSetters;
  }

  public boolean isAddGetters() {
    return addGetters;
  }

  public void setAddGetters(final boolean addGetters) {
    this.addGetters = addGetters;
  }

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

  public WriterWrapper getWriterWrapper() { return writerWrapper; }

  public void setWriterWrapper(final WriterWrapper writerWrapper) {
    if (writerWrapper == null) {
      throw new IllegalArgumentException("Writer wrapper cannot be null");
    }
    this.writerWrapper = writerWrapper;
  }

  public boolean isPrettifyNames() {
    return prettifyNames;
  }

  public void setPrettifyNames(final boolean prettifyNames) {
    this.prettifyNames = prettifyNames;
  }

  public String getFieldName(final Field field) {
    String name = field.getCanonicalName();
    return isPrettifyNames() ? Names.prettifiedName(name) : name;
  }

  public String getSafeFieldName(final Field field) {
    final String fieldName = getFieldName(field);
    if (JAVA_KEYWORDS.contains(fieldName)) {
      return fieldName.concat("Field");
    }
    return fieldName;
  }

  public Class<?> getJavaClass(final Type type) {
    Class<?> result = JavaPrimitiveTypes.javaClass(type);
    if (result == null) {
      String className = getCustomPrimitivesMapping().get(type.getName());
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

  public String getSequenceTypeName(final String itemJavaClass) {
    String collectionName = getSequenceCollectionName();
    if (collectionName != null) {
      return collectionName + "<" + itemJavaClass + ">";
    }
    return itemJavaClass + "[]";
  }


  /** Reserved java keywords. */
  private static final Set<String> JAVA_KEYWORDS = new HashSet<String>(Arrays.asList(
      "abstract",  "continue",  "for",  "new",  "switch",  "assert",  "default",  "goto",
      "package",  "synchronized",  "boolean",  "do",  "if",  "private",  "this",  "break",
      "double",  "implements",  "protected",  "throw",  "byte",  "else",  "import",  "public",
      "throws",  "case",  "enum",  "instanceof",  "return",  "transient",  "catch",  "extends",
      "int",  "short",  "try",  "char",  "final",  "interface",  "static",  "void",  "class",
      "finally",  "long",  "strictfp",  "volatile",  "const",  "float",  "native",  "super",  "while"
  ));

}
