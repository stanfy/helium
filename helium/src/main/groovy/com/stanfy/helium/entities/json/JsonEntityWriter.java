package com.stanfy.helium.entities.json;

import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.entities.Converter;
import com.stanfy.helium.entities.ConvertersPool;
import com.stanfy.helium.entities.EntityWriter;
import com.stanfy.helium.entities.TypedEntity;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes entity as JSON object.
 */
public class JsonEntityWriter implements EntityWriter {

  /** Output. */
  private final JsonWriter out;

  /** Types. */
  private final ConvertersPool<?, JsonWriter> converters;

  public JsonEntityWriter(final Writer out, final ConvertersPool<?, JsonWriter> converters) {
    this.out = new JsonWriter(out);
    this.out.setLenient(true);
    this.converters = converters;
  }

  @Override
  public void write(final TypedEntity<?> entity) throws IOException {
    Converter<?, ?, JsonWriter> converter = converters.getConverter(entity.getType());
    converter.write(out, entity.getValue());
  }

}
