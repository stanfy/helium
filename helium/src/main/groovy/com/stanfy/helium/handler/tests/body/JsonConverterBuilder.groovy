package com.stanfy.helium.handler.tests.body

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.RequestBody
import com.stanfy.helium.handler.tests.RequestBodyBuilder
import com.stanfy.helium.handler.tests.Utils
import com.stanfy.helium.internal.entities.EntitiesSink
import com.stanfy.helium.internal.entities.TypedEntity
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import groovy.transform.PackageScope
import okio.BufferedSink

/**
 * Used as default body builder.
 *
 * @see BuilderFactory
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
@PackageScope
class JsonConverterBuilder implements RequestBodyBuilder {
  @Override
  boolean canBuild(final Type bodyType) {
    return true // applicable to all
  }

  @Override
  RequestBody build(final TypeResolver types, final TypedEntity entity, String encoding) {
    return new RequestBody() {
      @Override
      public MediaType contentType() {
        return Utils.jsonType()
      }

      @Override
      public void writeTo(final BufferedSink sink) throws IOException {
        try {
          if (entity != null) {
            def contentType = Utils.jsonType()
            new EntitiesSink.Builder()
                .into(sink)
                .charset(encoding)
                .mediaType(contentType)
                .customAdapters(types.customWriters(contentType))
                .build()
                .write(entity)
          }
        } finally {
          sink.close()
        }
      }
    }
  }
}
