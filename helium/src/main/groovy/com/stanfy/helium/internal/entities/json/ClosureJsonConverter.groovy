package com.stanfy.helium.internal.entities.json

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.internal.entities.ConvertValueSyntaxException
import com.stanfy.helium.internal.entities.ValidationError
import com.stanfy.helium.model.Type

/**
 * Converts a primitive with custom reader/writer closures.
 */
class ClosureJsonConverter extends JsonPrimitiveConverter {

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

  public ClosureJsonConverter(final Type type, final Closure<?> reader, final Closure<?> writer) {
    super(type)
    this.writer = writer;
    this.reader = wrapWithOptionalNull(reader);
  }

  private static Closure<?> wrapWithOptionalNull(Closure<?> converter) {
    return { JsonReader reader ->
      if (reader.peek() == JsonToken.NULL) {
        return null
      }
      return converter(reader)
    }
  }

  @Override
  void write(final JsonWriter output, final Object value) throws IOException {
    writer.call(output, value)
  }

  private static boolean checkForEnd(final JsonReader input) {
    return !input.hasNext() || input.peek() == JsonToken.END_DOCUMENT;
  }

  @Override
  Object read(JsonReader input, List<ValidationError> errors) throws IOException {
    try {
      return reader.call(input)
    } catch (IllegalStateException e) {
      if (!checkForEnd(input)) {
        input.skipValue();
      }
      throw e;
    } catch (ConvertValueSyntaxException e) {
      // TODO all theses exceptions handlers should me moved to a convenient place
      throw new IllegalStateException(e.getMessage()); // do not skip
    } catch (IllegalArgumentException e) {
      if (!checkForEnd(input)) {
        input.skipValue();
      }
      throw new IllegalStateException("bad format: " + e.getMessage());
    }
  }

}

