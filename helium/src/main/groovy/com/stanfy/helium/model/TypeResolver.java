package com.stanfy.helium.model;

import com.squareup.okhttp.MediaType;
import com.stanfy.helium.internal.entities.ConvertersFactory;

/**
 * Operates with types.
 */
public interface TypeResolver {

  Type byName(String name);

  Type byGroovyClass(Class<?> clazz);

  void registerNewType(Type type);

  Iterable<Type> all();

  <I, O> ConvertersFactory<I, O> findConverters(MediaType mediaType);

}
