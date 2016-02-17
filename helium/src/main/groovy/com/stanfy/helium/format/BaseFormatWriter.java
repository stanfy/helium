package com.stanfy.helium.format;

import com.stanfy.helium.model.Type;

import java.io.IOException;

public abstract class BaseFormatWriter<O> extends BaseFormat<O, PrimitiveWriter<O>> implements FormatWriter {

  protected BaseFormatWriter(O o) {
    super(o, "write");
  }

  protected final O getOutput() {
    return core;
  }

  @Override
  public void value(Type type, Object value) throws IOException, ConvertValueSyntaxException {
    findAdapter(type).value(core, type, value);
  }

}
