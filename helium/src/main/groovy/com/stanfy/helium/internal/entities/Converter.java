package com.stanfy.helium.internal.entities;

import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.List;

/**
 * Can read/write data of some type with some format.
 * @param <T> type class
 * @param <O> output type
 * @param <I> input type
 */
public interface Converter<T extends Type, I, O> {

  String getFormat();

  T getType();

  void write(O output, Object value) throws IOException;

  Object read(I input, List<ValidationError> errors) throws IOException;

}
