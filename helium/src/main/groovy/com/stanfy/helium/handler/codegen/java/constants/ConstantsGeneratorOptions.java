package com.stanfy.helium.handler.codegen.java.constants;

import com.stanfy.helium.handler.codegen.java.JavaGeneratorOptions;
import com.stanfy.helium.model.Field;

import java.util.Locale;

/**
 * Constants generator options.
 */
public class ConstantsGeneratorOptions extends JavaGeneratorOptions {

  private static final long serialVersionUID = 1;

  public static ConstantsGeneratorOptions defaultOptions(final String packageName) {
    ConstantsGeneratorOptions converter = new ConstantsGeneratorOptions();
    converter.setPackageName(packageName);
    converter.setNameConverter(new ConstantNameConverter() {
      @Override
      public String constantFrom(final Field field) {
        return field.getCanonicalName().toUpperCase(Locale.US);
      }
    });
    return converter;
  }

  /** Constants name former. */
  private ConstantNameConverter nameConverter;

  public ConstantNameConverter getNameConverter() {
    return nameConverter;
  }

  public void setNameConverter(final ConstantNameConverter nameConverter) {
    if (nameConverter == null) {
      throw new IllegalArgumentException("Names converter cannot be null");
    }
    this.nameConverter = nameConverter;
  }

}
