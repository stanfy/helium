package com.stanfy.helium.handler.tests;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Response;
import com.stanfy.helium.dsl.MethodExecutionResult;
import com.stanfy.helium.internal.entities.TypedEntity;
import com.stanfy.helium.internal.entities.json.JsonConvertersPool;
import com.stanfy.helium.internal.entities.json.JsonEntityReader;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.TypeResolver;
import com.stanfy.helium.internal.utils.AssertionUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * HTTP response wrapper.
 */
class HttpResponseWrapper implements MethodExecutionResult {

  /** Empty response object. */
  private static final TypedEntity<?> EMPTY_RESPONSE = new TypedEntity<Type>(null, null);

  /** Types. */
  private final TypeResolver typeResolver;

  /** Response instance. */
  private final Response response;

  /** Type. */
  private final Type type;

  /** Headers. */
  private Map<String, String> httpHeaders;

  /** Body. */
  private TypedEntity<?> body;

  /** Errors. */
  private final List<AssertionError> errors = new LinkedList<AssertionError>();

  HttpResponseWrapper(final TypeResolver typeResolver, final Response response, final Type type) {
    this.typeResolver = typeResolver;
    this.type = type;
    this.response = response;
  }

  public Map<String, String> getHttpHeaders() {
    if (httpHeaders == null) {
      Headers headers = response.headers();
      int count = headers.size();
      httpHeaders = new LinkedHashMap<String, String>(count);
      for (int i = 0; i < count; i++) {
        httpHeaders.put(headers.name(i), headers.value(i));
      }
    }
    return httpHeaders;
  }

  public Object getBody() throws IOException {
    if (body == null) {
      mustSucceed();

      if (type != null) {
        // TODO: Support different content types.
        JsonEntityReader reader = new JsonEntityReader(
            response.body().charStream(),
            typeResolver.<JsonReader, JsonWriter>findConverters(JsonConvertersPool.JSON)
        );

        body = reader.read(type);

        try {
          AssertionUtils.assertCorrectEntity(body, response);
        } catch (AssertionError e) {
          errors.add(e);
        }

      } else {
        body = EMPTY_RESPONSE;
      }

    }
    return body.getValue();
  }

  @Override
  public int getStatusCode() {
    return response.code();
  }

  public void mustSucceed() {
    assertHttpExecution(true);
  }

  public void mustBeClientError() {
    assertHttpExecution(false);
  }

  public boolean isSuccessful() {
    return response.isSuccessful();
  }

  private void assertHttpExecution(final boolean success) {
    try {
      AssertionUtils.validateStatus(response, success);
    } catch (AssertionError e) {
      errors.add(e);
    }
  }

  public List<AssertionError> getInteractionErrors() {
    return errors;
  }

}
