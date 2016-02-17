package com.stanfy.helium.format;

import com.stanfy.helium.model.Type;

import java.io.IOException;

public interface PrimitiveWriter<O> {

  void value(O output, Type type, Object value) throws IOException, ConvertValueSyntaxException;

}
