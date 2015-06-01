package com.stanfy.helium.internal.dsl

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.stanfy.helium.DefaultType
import com.stanfy.helium.entities.ConvertersPool
import com.stanfy.helium.entities.json.ClosureJsonConverter
import com.stanfy.helium.entities.json.JsonConvertersPool
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver

/**
 * Default types resolver.
 */
class DefaultTypeResolver implements TypeResolver {

  /** Types map. */
  private final LinkedHashMap<String, Type> types = new LinkedHashMap<>()

  /** Converters. */
  private final JsonConvertersPool json = new JsonConvertersPool();

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
        return byName(DefaultType.DOUBLE.langName)
      case Float:
      case float:
        return byName(DefaultType.FLOAT.langName)
      case Integer:
      case int:
        return byName(DefaultType.INT32.langName)
      case Long:
      case long:
        return byName(DefaultType.INT64.langName)
      case String:
        return byName(DefaultType.STRING.langName)
      case byte[]:
        return byName(DefaultType.BYTES.langName)
      case boolean:
        return byName(DefaultType.BOOL.langName)
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
      case DefaultType.DOUBLE.langName:
        numReader = { JsonReader reader -> return reader.nextDouble() }
        break
      case DefaultType.FLOAT.langName:
        numReader = { JsonReader reader ->
          double doubleValue = reader.nextDouble()
          return (float)doubleValue;
        }
        break
      case DefaultType.INT32.langName:
        numReader = { JsonReader reader -> return reader.nextInt() }
        break
      case DefaultType.INT64.langName:
        numReader = { JsonReader reader -> return reader.nextLong() }
        break

      case DefaultType.BOOL.langName:
        json.addConverter(type.name, new ClosureJsonConverter(
            type,
            { JsonReader input -> return input.nextBoolean() },
            { JsonWriter output, Object value -> output.value((Boolean)value) }
        ))
        break

      case DefaultType.STRING.langName:
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
  def <I, O> ConvertersPool<I, O> findConverters(final String format) {
    if (JsonConvertersPool.JSON == format) {
      return json as ConvertersPool<I, O>;
    }
    throw new UnsupportedOperationException("Format " + format + " is not supported")
  }

}
