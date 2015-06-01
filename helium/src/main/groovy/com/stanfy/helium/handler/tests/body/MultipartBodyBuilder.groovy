package com.stanfy.helium.handler.tests.body

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.MultipartBuilder
import com.squareup.okhttp.RequestBody
import com.stanfy.helium.entities.ByteArrayEntity
import com.stanfy.helium.entities.TypedEntity
import com.stanfy.helium.handler.tests.RequestBodyBuilder
import com.stanfy.helium.model.MultipartType
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.TypeResolver
import groovy.transform.PackageScope

import static com.stanfy.helium.handler.tests.Utils.bytesType
import static com.stanfy.helium.handler.tests.Utils.writeEntityWithConverters

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

        final Type type = types.byGroovyClass(value.getClass())
        TypedEntity wrappedEntity = new TypedEntity(type, value)
        StringWriter out = new StringWriter()
        try {
          writeEntityWithConverters(wrappedEntity, out, types)
        } catch (IOException e) {
          throw new RuntimeException(e)
        }

        mb.addFormDataPart(key, out.toString())
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
