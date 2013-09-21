package com.stanfy.helium.dsl

import com.stanfy.helium.model.Type

/**
 * Default types resolver.
 */
class DefaultTypeResolver implements TypeResolver {

  /** Types map. */
  private final HashMap<String, Type> types = new HashMap<>()

  DefaultTypeResolver() {
    // configure standard types
    registerNewType(new Type(name : "double"))
    registerNewType(new Type(name : "float"))
    registerNewType(new Type(name : "int32"))
    registerNewType(new Type(name : "int64"))
    registerNewType(new Type(name : "bool"))
    registerNewType(new Type(name : "string"))
    registerNewType(new Type(name : "bytes"))
  }

  @Override
  Type byName(final String name) {
    Type type = types[name]
    if (type) { return type }
    throw new IllegalArgumentException("Unknown type $name")
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

}
