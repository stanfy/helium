package com.stanfy.helium.handler.codegen.java.entity;

import com.stanfy.helium.handler.codegen.java.JavaGeneratorOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Options for POJO generator.
 */
public class EntitiesGeneratorOptions extends JavaGeneratorOptions {

  private static final long serialVersionUID = 1;

  /** Field modifiers. */
  private Set<Modifier> fieldModifiers = Collections.emptySet();

  /** Whether to generate setters. */
  private boolean addSetters;

  /** Whether to generate getters. */
  private boolean addGetters;

  /** Whether to generate toString() method. */
  private boolean addToString = true;

  /** Writer  */
  private Writers.WriterWrapper writerWrapper = Writers.chain();

  /** Default options. */
  public static EntitiesGeneratorOptions defaultOptions(final String packageName) {
    EntitiesGeneratorOptions options = new EntitiesGeneratorOptions();
    options.setFieldModifiers(new HashSet<Modifier>(Arrays.asList(Modifier.PUBLIC)));
    options.setPackageName(packageName);
    return options;
  }

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

  public boolean isAddToString() {
    return addToString;
  }

  public void setAddToString(final boolean addToString) {
    this.addToString = addToString;
  }

  public Writers.WriterWrapper getWriterWrapper() {
    return writerWrapper;
  }

  public void setWriterWrapper(final Writers.WriterWrapper writerWrapper) {
    if (writerWrapper == null) {
      throw new IllegalArgumentException("Writer wrapper cannot be null");
    }
    this.writerWrapper = writerWrapper;
  }


}
