package com.stanfy.helium.dsl

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.entities.ConverterFactory
import com.stanfy.helium.entities.json.ClosureJsonConverter
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

}
