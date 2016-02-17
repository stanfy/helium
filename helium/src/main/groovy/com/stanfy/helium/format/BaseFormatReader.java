package com.stanfy.helium.format;

import com.stanfy.helium.model.Type;

import java.io.IOException;

public abstract class BaseFormatReader<I> extends BaseFormat<I, PrimitiveReader<I>> implements FormatReader {

  protected BaseFormatReader(I input) {
    super(input, "read");
  }

  protected final I getInput() {
    return core;
  }

  @Override
  public Object nextValue(Type type) throws IOException, ConvertValueSyntaxException {
    return findAdapter(type).value(core, type);
  }

}
