package com.stanfy.helium.handler.tests.body

import com.squareup.okhttp.FormEncodingBuilder
import com.squareup.okhttp.RequestBody
import com.stanfy.helium.internal.entities.TypedEntity
import com.stanfy.helium.handler.tests.RequestBodyBuilder
import com.stanfy.helium.model.FormType
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import groovy.transform.PackageScope

/**
 * {@link RequestBodyBuilder} that builds <i>form-url-encoded</i>
 * body.
 *
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
@PackageScope
class FormBodyBuilder implements RequestBodyBuilder {

  @Override
  boolean canBuild(final Type bodyType) {
    return bodyType instanceof FormType
  }

  @Override
  RequestBody build(TypeResolver types, final TypedEntity entity, String encoding) {
    FormEncodingBuilder formBuilder = new FormEncodingBuilder()
    final Map<String, Object> map = (Map<String, Object>) entity.getValue()
    for (String key : map.keySet()) {
      formBuilder.add(key, String.valueOf(map.get(key)))
    }
    return formBuilder.build()
  }
}
