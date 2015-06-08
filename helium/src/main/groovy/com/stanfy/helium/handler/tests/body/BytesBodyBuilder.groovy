package com.stanfy.helium.handler.tests.body

import com.squareup.okhttp.RequestBody
import com.stanfy.helium.entities.ByteArrayEntity
import com.stanfy.helium.entities.TypedEntity
import com.stanfy.helium.handler.tests.RequestBodyBuilder
import com.stanfy.helium.model.DataType
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import groovy.transform.PackageScope

import static com.stanfy.helium.handler.tests.Utils.bytesType

/**
 * {@link RequestBodyBuilder} that builds body from raw bytes.
 *
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
@PackageScope
class BytesBodyBuilder implements RequestBodyBuilder {

  @Override
  boolean canBuild(final Type bodyType) {
    return bodyType instanceof DataType
  }

  @Override
  RequestBody build(TypeResolver types, final TypedEntity entity, String encoding) {
    byte[] arr
    if (entity.getValue() instanceof byte[]) {
      arr = (byte[]) entity.getValue()
    } else if (entity.getValue() instanceof ByteArrayEntity) {
      arr = ((ByteArrayEntity) entity.getValue()).getBytes()
    } else {
      throw new IllegalArgumentException("Type " + entity.getValue().getClass() + " is not supported for raw data input.")
    }

    return RequestBody.create(bytesType(), arr)
  }
}
