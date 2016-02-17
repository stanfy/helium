package com.stanfy.helium.format;

import com.stanfy.helium.model.Type;

import java.io.IOException;

public interface PrimitiveReader<I> {

  Object value(I input, Type type) throws IOException, ConvertValueSyntaxException;

}
