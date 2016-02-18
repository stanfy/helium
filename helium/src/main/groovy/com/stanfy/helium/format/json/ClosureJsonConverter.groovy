package com.stanfy.helium.format.json

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.format.PrimitiveReader
import com.stanfy.helium.format.PrimitiveWriter
import com.stanfy.helium.model.Type
import groovy.transform.CompileStatic

/**
 * Converts a primitive with custom reader/writer closures.
 */
@CompileStatic
class ClosureJsonConverter implements PrimitiveReader<JsonReader>, PrimitiveWriter<JsonWriter> {

  public static final Closure<?> AS_STRING_READER = { JsonReader reader ->
    JsonToken nextToken = reader.peek();
    if (nextToken != JsonToken.STRING) {
      throw new IllegalArgumentException("not a string");
    }
    return reader.nextString()
  }

  public static final Closure<?> AS_STRING_WRITER = { JsonWriter output, Object value ->
    if (value == null) {
      output.nullValue()
    } else {
      output.value((String)value)
    }
  }

  /** Writer closure. */
  final Closure<?> writer
  /** Reader closure. */
  final Closure<?> reader

  public ClosureJsonConverter(final Closure<?> reader, final Closure<?> writer) {
    this.writer = writer;
    this.reader = wrapWithOptionalNull(reader);
  }

  @Override
  Object value(JsonReader input, Type type) throws IOException {
    try {
      return reader.call(input)
    } catch (IllegalStateException | IllegalArgumentException e) {
      if (!checkForEnd(input)) {
        input.skipValue();
      }
      throw e;
    }
  }

  @Override
  void value(JsonWriter output, Type type, Object value) throws IOException {
    writer.call(output, value)
  }

  private static Closure<?> wrapWithOptionalNull(Closure<?> converter) {
    return { JsonReader reader ->
      if (reader.peek() == JsonToken.NULL) {
        return null
      }
      return converter(reader)
    }
  }

  private static boolean checkForEnd(final JsonReader input) {
    return !input.hasNext() || input.peek() == JsonToken.END_DOCUMENT;
  }

}

