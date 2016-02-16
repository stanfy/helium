package com.stanfy.helium.internal.entities.json;

import com.google.gson.stream.JsonWriter;
import com.squareup.okhttp.MediaType;
import com.stanfy.helium.internal.entities.Converter;
import com.stanfy.helium.internal.entities.ConvertersFactory;
import com.stanfy.helium.internal.entities.EntitiesSink;
import com.stanfy.helium.internal.entities.TypedEntity;
import okio.Okio;
import okio.Sink;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Provides a JSON format sink.
 */
public class JsonSinkProvider implements EntitiesSink.Factory {

  @Override
  public boolean supportsMediaType(MediaType type) {
    return "json".equals(type.subtype());
  }

  @Override
  public EntitiesSink create(final Sink sink, final Charset charset,
                             final ConvertersFactory cFactory) {
    return new EntitiesSink() {
      @SuppressWarnings("unchecked")
      @Override
      public void write(TypedEntity<?> entity) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(Okio.buffer(sink).outputStream(), charset);
        try {
          final JsonWriter writer = new JsonWriter(out);
          writer.setLenient(true);
          Converter<?, ?, JsonWriter> converter = cFactory.getConverter(entity.getType());
          converter.write(writer, entity.getValue());
        } finally {
          out.close();
        }
      }
    };
  }
}
