package com.stanfy.helium.handler.tests;

import com.stanfy.helium.internal.entities.EntitiesSink;
import com.stanfy.helium.internal.entities.TypedEntity;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Writes simple message to HTTP parameters query.
 */
class HttpParamsWriter implements EntitiesSink {

  /** Output. */
  private final Writer out;

  /** Encoding. */
  private final String encoding;

  public HttpParamsWriter(final Writer out, final String encoding) {
    this.out = out;
    this.encoding = encoding;
  }

  private void writePair(String name, String value) throws IOException {
    out.write(URLEncoder.encode(name, encoding));
    out.write("=");
    out.write(URLEncoder.encode(value, encoding));
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
      count--;
      if (f.isSequence()) {
        @SuppressWarnings("unchecked")
        List<Object> array = (List<Object>) values.get(name);
        int arrayCount = array.size();
        for (Object value : array) {
          writePair(name, String.valueOf(value));
          arrayCount--;
          if (count != 0 || arrayCount != 0) {
            out.write('&');
          }
        }
      } else {
        writePair(name, String.valueOf(values.get(name)));
        if (count != 0) {
          out.write('&');
        }
      }
    }

  }
}
