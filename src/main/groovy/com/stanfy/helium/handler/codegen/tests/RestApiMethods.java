package com.stanfy.helium.handler.codegen.tests;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Base class for generated REST API tests.
 */
public class RestApiMethods {

  private final HttpClient client = HttpClientBuilder.create()
      .build();

  protected HttpResponse send(final HttpRequestBase request) throws Exception {
    return client.execute(request);
  }

  protected void validateStatus(final HttpResponse response, final boolean success) {
    if (success) {
      response.getStatusLine().getStatusCode()
    }
  }

}
