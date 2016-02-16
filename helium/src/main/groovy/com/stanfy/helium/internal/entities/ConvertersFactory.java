package com.stanfy.helium.internal.entities;

import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.constraints.ConstrainedType;

import java.util.LinkedHashMap;

/**
 * Resolves converters for particular types. Has support for constrained types.
 */
public abstract class ConvertersFactory<I, O> {

  /** Converters. */
  private LinkedHashMap<String, Converter<?, I, O>> converters = new LinkedHashMap<String, Converter<?, I, O>>();

  public abstract String getFormat();

  public void addConverter(final String typeName, final Converter<?, I, O> converter) {
    converters.put(typeName, converter);
  }

  @SuppressWarnings("unchecked")
  public <T extends Type> Converter<T, I, O> getConverter(final T type) {
    if (type instanceof ConstrainedType) {
      return (Converter<T, I, O>) new ConstrainedTypeConverter<I, O>((ConstrainedType) type, this);
    }
    Converter<T, I, O> result = (Converter<T, I, O>) converters.get(type.getName());
    if (result == null) {
      throw new UnsupportedOperationException("Cannot find how to convert " + type + " to " + getFormat() + " format");
    }
    return result;
  }

}
