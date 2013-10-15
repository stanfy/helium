package com.stanfy.helium.dsl.scenario;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.entities.json.JsonConverterFactory;
import com.stanfy.helium.entities.json.JsonEntityReader;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.TypeResolver;
import com.stanfy.helium.utils.AssertionUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP response wrapper.
 */
public class HttpResponseWrapper {

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
  private TypedEntity body;

  public HttpResponseWrapper(final TypeResolver typeResolver, final HttpRequest request, final HttpResponse response, final String encoding, final Type type) {
    this.typeResolver = typeResolver;
    this.request = request;
    this.response = response;
    this.encoding = encoding;
    this.type = type;
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
      JsonEntityReader reader = new JsonEntityReader(
          new InputStreamReader(new BufferedInputStream(response.getEntity().getContent()), encoding),
          typeResolver.<JsonReader, JsonWriter>findConverters(JsonConverterFactory.JSON)
      );
      body = reader.read(type);
      AssertionUtils.assertCorrectEntity(body, request, response);
    }
    return body.getValue();
  }

  public void mustSucceed() {
    AssertionUtils.validateStatus(request, response, true);
  }
  public void mustBeClientError() {
    AssertionUtils.validateStatus(request, response, false);
  }

}
