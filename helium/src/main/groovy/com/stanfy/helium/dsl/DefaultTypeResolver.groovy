package com.stanfy.helium.dsl

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.entities.ConverterFactory
import com.stanfy.helium.entities.ValidationError
import com.stanfy.helium.entities.json.JsonConverterFactory
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver

/**
 * Default types resolver.
 */
class DefaultTypeResolver implements TypeResolver {

  /** Types map. */
  private final LinkedHashMap<String, Type> types = new LinkedHashMap<>()

  /** Converters. */
  private final JsonConverterFactory json = new JsonConverterFactory();

  @Override
  Type byName(final String name) {
    Type type = types[name]
    if (type) { return type }
    throw new IllegalArgumentException("Unknown type $name. Types registered: ${types.keySet()}")
  }

  @Override
  Type byGroovyClass(final Class<?> clazz) {
    switch (clazz) {
      case Double:
      case double:
        return byName("double")
      case Float:
      case float:
        return byName("float")
      case Integer:
      case int:
        return byName("int32")
      case Long:
      case long:
        return byName("int64")
      case String:
        return byName("string")
      case byte[]:
        return byName("bytes")
      case boolean:
        return byName("bool")
      default:
        throw new IllegalArgumentException("Unknown type $clazz")
    }
  }

  @Override
  void registerNewType(final Type type) {
    if (types[type.name]) {
      throw new IllegalArgumentException("Cannot register type $type. Type $type.name is already defined: ${types[type.name]}")
    }
    types[type.name] = type

    Closure<?> numReader
    switch (type.name) {
      case "double":
        numReader = { JsonReader reader -> return reader.nextDouble() }
        break
      case "float":
        numReader = { JsonReader reader ->
          double doubleValue = reader.nextDouble()
          return (float)doubleValue;
        }
        break
      case "int32":
        numReader = { JsonReader reader -> return reader.nextInt() }
        break
      case "int64":
        numReader = { JsonReader reader -> return reader.nextLong() }
        break

      case "bool":
        json.addConverter(type.name, new ClosureJsonConverter(
            type,
            { JsonReader input -> return input.nextBoolean() },
            { JsonWriter output, Object value -> output.value((Boolean)value) }
        ))
        break

      case "string":
        json.addConverter(type.name, new ClosureJsonConverter(
            type,
            ClosureJsonConverter.AS_STRING_READER,
            ClosureJsonConverter.AS_STRING_WRITER
        ))
        break
    }

    if (numReader) {
      json.addConverter(type.name, new ClosureJsonConverter(
          type,
          numReader,
          { JsonWriter output, Object value ->
            output.value((Number)value)
          }
      ))
    }

  }

  private static Closure<?> wrapWithOptionalNull(Closure<?> converter) {
    return { JsonReader reader ->
      if (reader.peek() == JsonToken.NULL) {
        return null
      }
      return converter(reader)
    }
  }

  Iterable<Type> all() {
    return Collections.unmodifiableCollection(types.values())
  }

  @Override
  def <I, O> ConverterFactory<I, O> findConverters(final String format) {
    if (JsonConverterFactory.JSON == format) {
      return json as ConverterFactory<I, O>;
    }
    throw new UnsupportedOperationException("Format " + format + " is not supported")
  }

  public static class ClosureJsonConverter extends JsonConverterFactory.JsonPrimitiveConverter {

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

    /** Closure. */
    final Closure<?> writer;
    /** Reader. */
    final Closure<?> reader;

    ClosureJsonConverter(final Type type, final Closure<?> reader, final Closure<?> writer) {
      super(type)
      this.writer = writer;
      this.reader = wrapWithOptionalNull(reader);
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
      } catch (IllegalArgumentException e) {
        if (!checkForEnd(input)) {
          input.skipValue();
        }
        throw new IllegalStateException("bad format: " + e.getMessage());
      }
    }

  }

}
