package com.stanfy.helium.dsl.scenario;

import com.stanfy.helium.entities.EntityReader;
import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.entities.json.GsonEntityReader;
import com.stanfy.helium.model.Type;
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

  public HttpResponseWrapper(final HttpRequest request, final HttpResponse response, final String encoding, final Type type) {
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
      EntityReader reader = new GsonEntityReader(new InputStreamReader(new BufferedInputStream(response.getEntity().getContent()), encoding));
      body = reader.read(type);
      AssertionUtils.assertCorrectEntity(body, request);
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
