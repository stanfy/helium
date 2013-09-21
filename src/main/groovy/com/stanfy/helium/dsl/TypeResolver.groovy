package com.stanfy.helium.dsl

import com.stanfy.helium.model.Type

/**
 * Operates with types.
 */
interface TypeResolver {

  Type byName(String name)

  Type byGroovyClass(Class<?> clazz)

  void registerNewType(Type type)

  Iterable<Type> all()

}
