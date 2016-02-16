package com.stanfy.helium.handler.tests.body

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.MultipartBuilder
import com.squareup.okhttp.RequestBody
import com.stanfy.helium.handler.tests.RequestBodyBuilder
import com.stanfy.helium.handler.tests.Utils
import com.stanfy.helium.internal.entities.ByteArrayEntity
import com.stanfy.helium.internal.entities.EntitiesSink
import com.stanfy.helium.internal.entities.TypedEntity
import com.stanfy.helium.model.MultipartType
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import groovy.transform.PackageScope
import okio.Buffer

import static com.stanfy.helium.handler.tests.Utils.bytesType

/**
 * {@link RequestBodyBuilder} that builds multipart body.
 *
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
@PackageScope
class MultipartBodyBuilder implements RequestBodyBuilder {

  @Override
  boolean canBuild(final Type bodyType) {
    return bodyType instanceof MultipartType
  }

  @Override
  RequestBody build(final TypeResolver types, final TypedEntity requestBody, String encoding) {
    RequestBody body
    final MultipartBuilder mb = new MultipartBuilder()
    final Map<String, Object> map = (Map<String, Object>) requestBody.getValue()

    for (String key : map.keySet()) {
      final Object value = map.get(key)
      if (value instanceof byte[]) {
        final byte[] bytes = (byte[]) value

        mb.addFormDataPart(key, null, RequestBody.create(bytesType(), bytes))
      } else if (value instanceof ByteArrayEntity) {
        final byte[] bytes = ((ByteArrayEntity) value).getBytes()

        mb.addFormDataPart(key, null, RequestBody.create(bytesType(), bytes))
      } else if (value instanceof File) {
        final File file = (File) value
        // TODO check file extension to guess it's media type.

        mb.addFormDataPart(key, file.getName(), RequestBody.create(bytesType(), file))
      } else  {

        Buffer out = new Buffer()
        final Type type = types.byGroovyClass(value.getClass())
        TypedEntity wrappedEntity = new TypedEntity(type, value)
        new EntitiesSink.Builder()
            .into(out)
            .types(types)
            // TODO: This must be configurable!
            .mediaType(Utils.jsonType())
            .build()
            .write(wrappedEntity)

        mb.addFormDataPart(key, out.readUtf8())
      }

    }

    mb.type(getMultipartType(requestBody))
    body = mb.build()
    return body
  }

  public static MediaType getMultipartType(TypedEntity requestBody) {
    MediaType.parse("multipart/" + (requestBody.getType() as MultipartType).subtype.representation())
  }
}
