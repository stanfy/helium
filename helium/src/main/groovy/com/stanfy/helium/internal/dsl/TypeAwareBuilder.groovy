package com.stanfy.helium.internal.dsl

import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import groovy.transform.CompileStatic

/**
 * Base builder that can resolve types.
 *
 * @see MultipartBuilder
 * @see FieldsBuilder
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
@CompileStatic
class TypeAwareBuilder {

  protected final TypeResolver typeResolver

  TypeAwareBuilder(final TypeResolver typeResolver) {
    this.typeResolver = typeResolver
  }

  protected Type resolveType(final Object arg) {
    if (arg == null) {
      throw new IllegalArgumentException("Type is not specified");
    }

    if (arg instanceof Type) {
      return (Type)arg
    }

    final Type type
    if (arg instanceof Class) {
      type = typeResolver.byGroovyClass((Class<?>)arg)
    } else {
      type = typeResolver.byName("$arg")
    }
    return type
  }
}
