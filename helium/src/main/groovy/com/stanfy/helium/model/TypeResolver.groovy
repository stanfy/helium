package com.stanfy.helium.model

/**
 * Operates with types.
 */
interface TypeResolver {

  Type byName(String name)

  Type byGroovyClass(Class<?> clazz)

  void registerNewType(Type type)

  Iterable<Type> all()

  Class<?> toGroovyClass(Type type)

}
