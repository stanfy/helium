package com.stanfy.helium.dsl.scenario;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
          result.setEntity(new StringEntity(IOUtils.toString(response.getEntity().getContent(), encoding), ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), encoding)));
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
      // TODO: support different content types
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
