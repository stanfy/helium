package com.stanfy.helium.model;

import com.squareup.okhttp.MediaType;
import com.stanfy.helium.format.PrimitiveReader;
import com.stanfy.helium.format.PrimitiveWriter;

import java.util.Map;

/**
 * Operates with types.
 */
public interface TypeResolver {

  Type byName(String name);

  Type byGroovyClass(Class<?> clazz);

  void registerNewType(Type type);

  Iterable<Type> all();

  Map<Type, PrimitiveReader<?>> customReaders(MediaType mediaType);

  Map<Type, PrimitiveWriter<?>> customWriters(MediaType mediaType);
}
