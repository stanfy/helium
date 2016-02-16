package com.stanfy.helium.internal.entities;

import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.List;

/**
 * Base type for serializers.
 * @param <O> output type
 * @param <I> input type
 */
abstract class BaseTypeConverter<I, O> {

  /** Format. */
  private final String format;

  public BaseTypeConverter(final String format) {
    this.format = format;
  }

  public String getFormat() {
    return format;
  }

  public abstract ConvertersFactory<I, O> getPool();

  protected void writeValue(final Type type, final Object value, final O output) throws IOException {
    Converter<Type, I, O> converter = getTypeConverter(type);
    converter.write(output, value);
  }

  protected Object readValue(final Type type, final Field field, final I input,
                             final List<ValidationError> errors) throws IOException {
    Converter<Type, I, O> converter = getTypeConverter(type);
    try {
      return converter.read(input, errors);
    } catch (IllegalStateException e) {
      errors.add(new ValidationError(type, field,
          "Cannot parse " + getFormat() + " input for value of type " + type + ". " + e.getMessage()));
      return null;
    }
  }

  protected Converter<Type, I, O> getTypeConverter(final Type type) {
    ConvertersFactory<I, O> pool = getPool();
    Converter<Type, I, O> converter = pool.getConverter(type);
    if (converter == null) {
      throw new IllegalStateException("Converter of type " + type + " for format " + getFormat() + " not found");
    }
    return converter;
  }

}
