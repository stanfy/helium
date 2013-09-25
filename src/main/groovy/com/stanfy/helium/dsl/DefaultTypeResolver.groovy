package com.stanfy.helium.dsl

import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver

/**
 * Default types resolver.
 */
class DefaultTypeResolver implements TypeResolver {

  /** Types map. */
  private final HashMap<String, Type> types = new HashMap<>()

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
      default:
        throw new IllegalArgumentException("Unknown type $clazz")
    }
  }

  @Override
  void registerNewType(final Type type) {
    if (types[type.name]) {
      throw new IllegalArgumentException("Cannot register tyoe $type. Type $type.name is already defined: ${types[type.name]}")
    }
    types[type.name] = type
  }

  Iterable<Type> all() {
    return Collections.unmodifiableCollection(types.values())
  }

}
