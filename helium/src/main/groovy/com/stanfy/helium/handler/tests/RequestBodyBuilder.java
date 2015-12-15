package com.stanfy.helium.handler.tests;

import com.squareup.okhttp.RequestBody;
import com.stanfy.helium.internal.entities.TypedEntity;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.TypeResolver;

/**
 * Represents converter from {@link TypedEntity}
 * and {@link com.squareup.okhttp.RequestBody}
 * that may check if it's applicable and can build RequestBody from given entity.
 *
 * @see HttpExecutor
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
public interface RequestBodyBuilder {

  boolean canBuild(final Type bodyType);

  RequestBody build(final TypeResolver types, final TypedEntity entity, final String encoding);

}
