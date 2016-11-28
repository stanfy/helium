package com.stanfy.helium.internal.dsl

import com.squareup.okhttp.MediaType
import com.stanfy.helium.DefaultType
import com.stanfy.helium.format.PrimitiveReader
import com.stanfy.helium.format.PrimitiveWriter
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver

/**
 * Default types resolver.
 */
class DefaultTypeResolver implements TypeResolver {

  /** Types map. */
  private final LinkedHashMap<String, Type> types = new LinkedHashMap<>()

  private final List<FormatCollection<PrimitiveReader<?>>> readers = []
  private final List<FormatCollection<PrimitiveWriter<?>>> writers = []

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
  }

  Iterable<Type> all() {
    return Collections.unmodifiableCollection(types.values())
  }

  @Override
  Map<Type, PrimitiveReader<?>> customReaders(MediaType mediaType) {
    def res = readers.find() { mediaType.subtype().endsWith(it.format) }
    if (!res) {
      return Collections.emptyMap()
    }
    return Collections.unmodifiableMap(res.adapters)
  }

  @Override
  Map<Type, PrimitiveWriter<?>> customWriters(MediaType mediaType) {
    def res = writers.find() { mediaType.subtype().endsWith(it.format) }
    if (!res) {
      return Collections.emptyMap()
    }
    return Collections.unmodifiableMap(res.adapters)
  }

  void addTypeReader(String format, Type type, PrimitiveReader<?> reader) {
    def col = readers.find() { format == it.format }
    if (!col) {
      col = new FormatCollection<PrimitiveReader<?>>(format)
      readers.add col
    }
    col.adapters[type] = reader
  }

  void addTypeWriter(String format, Type type, PrimitiveWriter<?> writer) {
    def col = writers.find() { format == it.format }
    if (!col) {
      col = new FormatCollection<PrimitiveWriter<?>>(format)
      writers.add col
    }
    col.adapters[type] = writer
  }

  private static final class AdapterCollection<T> extends LinkedHashMap<Type, T> { }

  private static final class FormatCollection<T> {
    final String format
    final AdapterCollection<T> adapters = new AdapterCollection<>()

    FormatCollection(String format) {
      this.format = format
    }
  }

}
