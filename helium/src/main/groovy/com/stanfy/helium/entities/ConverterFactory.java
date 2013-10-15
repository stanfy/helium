package com.stanfy.helium.entities;

import com.stanfy.helium.model.Type;

import java.util.LinkedHashMap;

/**
 * Creates converters for some format.
 */
public abstract class ConverterFactory<I, O> {

  /** Converters. */
  private LinkedHashMap<String, Converter<?, I, O>> converters = new LinkedHashMap<String, Converter<?, I, O>>();

  public abstract String getFormat();

  public void addConverter(final String typeName, final Converter<?, I, O> converter) {
    converters.put(typeName, converter);
  }

  @SuppressWarnings("unchecked")
  public <T extends Type> Converter<T, I, O> getConverter(final T type) {
    Converter<T, I, O> result = (Converter<T, I, O>) converters.get(type.getName());
    if (result == null) {
      throw new UnsupportedOperationException("Cannot find how to convert " + type + " to " + getFormat() + " format");
    }
    return result;
  }

}
