package com.stanfy.helium.model;

import com.stanfy.helium.entities.ConverterFactory;

/**
 * Operates with types.
 */
public interface TypeResolver {

  Type byName(String name);

  Type byGroovyClass(Class<?> clazz);

  void registerNewType(Type type);

  Iterable<Type> all();

  Class<?> toJavaClass(Type type);

  <I, O> ConverterFactory<I, O> findConverters(final String format);

}
