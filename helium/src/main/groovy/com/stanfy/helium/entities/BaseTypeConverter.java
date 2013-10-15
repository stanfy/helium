package com.stanfy.helium.entities;

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

  public String getFormat() { return format; }

  public abstract ConverterFactory<I, O> getFactory();

  protected void writeValue(final Type type, final Object value, final O output) throws IOException {
    Converter<Type, I, O> converter = getTypeConverter(type);
    converter.write(output, value);
  }

  protected Object readValue(final Type type, final I input, final List<ValidationError> errors) throws IOException {
    Converter<Type, I, O> converter = getTypeConverter(type);
    try {
      return converter.read(input, errors);
    } catch (IllegalStateException e) {
      errors.add(new ValidationError(type, "Cannot parse " + getFormat() + " input for value of type " + type));
      return null;
    }
  }

  protected Converter<Type, I, O> getTypeConverter(final Type type) {
    ConverterFactory<I, O> factory = getFactory();
    Converter<Type, I, O> converter = factory.getConverter(type);
    if (converter == null) {
      throw new IllegalStateException("Converter of type " + type + " for format " + getFormat() + " not found");
    }
    return converter;
  }

}
