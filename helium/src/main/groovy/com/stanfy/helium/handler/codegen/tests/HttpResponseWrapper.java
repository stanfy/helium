package com.stanfy.helium.handler.codegen.tests;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.dsl.scenario.MethodExecutionResult;
import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.entities.json.JsonConverterFactory;
import com.stanfy.helium.entities.json.JsonEntityReader;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.TypeResolver;
import com.stanfy.helium.utils.AssertionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
  private final HttpResponse response;

  /** Request. */
  private final HttpRequest request;

  /** Encoding. */
  private final String encoding;

  /** Type. */
  private final Type type;

  /** Headers. */
  private Map<String, String> httpHeaders;

  /** Body. */
  private TypedEntity<?> body;

  /** Errors. */
  private final List<AssertionError> errors = new LinkedList<AssertionError>();

  HttpResponseWrapper(final TypeResolver typeResolver, final HttpRequest request, final HttpResponse response,
                      final String encoding, final Type type) {
    this.typeResolver = typeResolver;
    this.request = request;
    this.encoding = encoding;
    this.type = type;
    this.response = wrapResponse(response);
  }

  private HttpResponse wrapResponse(final HttpResponse response) {
    final HttpEntity responseEntity = response.getEntity();
    if (responseEntity != null) {
      InputStream content;
      try {
        content = responseEntity.getContent();
        if (content != null) {
          final HttpResponse result = new BasicHttpResponse(response.getStatusLine());
          result.setHeaders(response.getAllHeaders());
          // TODO: support different content types
          result.setEntity(new StringEntity(
              IOUtils.toString(response.getEntity().getContent(), encoding),
              ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), encoding)
          ));
          IOUtils.closeQuietly(content);
          return result;
        }
      } catch (final IOException e) {
        //ignore
      }
    }

    return response;
  }

  public Map<String, String> getHttpHeaders() {
    if (httpHeaders == null) {
      Header[] headers = response.getAllHeaders();
      httpHeaders = new LinkedHashMap<String, String>(headers.length);
      for (Header header : headers) {
        httpHeaders.put(header.getName(), header.getValue());
      }
    }
    return httpHeaders;
  }

  public Object getBody() throws IOException {
    if (body == null) {
      mustSucceed();

      if (type != null) {
        // TODO: support different content types
        JsonEntityReader reader = new JsonEntityReader(
            new InputStreamReader(new BufferedInputStream(
                response.getEntity().getContent()), encoding
            ),
            typeResolver.<JsonReader, JsonWriter>findConverters(JsonConverterFactory.JSON)
        );

        body = reader.read(type);

        try {
          AssertionUtils.assertCorrectEntity(body, request, response);
        } catch (AssertionError e) {
          errors.add(e);
        }

      } else {
        body = EMPTY_RESPONSE;
      }

    }
    return body.getValue();
  }

  public void mustSucceed() {
    assertHttpExecution(true);
  }

  public void mustBeClientError() {
    assertHttpExecution(false);
  }

  public boolean isSuccessful() {
    int statusCode = response.getStatusLine().getStatusCode();
    return statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES;
  }

  private void assertHttpExecution(final boolean success) {
    try {
      AssertionUtils.validateStatus(request, response, success);
    } catch (AssertionError e) {
      errors.add(e);
    }
  }

  @Override
  public List<AssertionError> getInteractionErrors() {
    return errors;
  }

}
