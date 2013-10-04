package com.stanfy.helium.handler.codegen.tests;

import com.stanfy.helium.entities.EntityWriter;
import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Writes simple message to HTTP parameters query.
 */
class HttpParamsWriter implements EntityWriter {

  /** Output. */
  private final Writer out;

  /** Encoding. */
  private final String encoding;

  public HttpParamsWriter(final Writer out, final String encoding) {
    this.out = out;
    this.encoding = encoding;
  }

  @Override
  public void write(final TypedEntity entity) throws IOException {
    if (!(entity.getType() instanceof Message)) {
      throw new IllegalArgumentException("Can serialize messages only");
    }

    Message msg = (Message) entity.getType();
    @SuppressWarnings("unchecked")
    Map<String, Object> values = (Map<String, Object>) entity.getValue();

    int count = msg.getFields().size();
    for (Field f : msg.getFields()) {
      String name = f.getName();
      if (!f.getType().isPrimitive()) {
        throw new IllegalStateException("Field " + name + " is not primitive");
      }
      Object value = values.get(name);
      out.write(URLEncoder.encode(name, encoding));
      out.write("=");
      out.write(URLEncoder.encode(String.valueOf(value), encoding));
      count--;
      if (count != 0) {
        out.write('&');
      }
    }

  }
}
