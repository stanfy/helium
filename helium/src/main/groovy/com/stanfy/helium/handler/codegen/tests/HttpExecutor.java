package com.stanfy.helium.handler.codegen.tests;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.dsl.scenario.ScenarioExecutor;
import com.stanfy.helium.dsl.scenario.ServiceMethodRequestValues;
import com.stanfy.helium.entities.json.JsonConverterFactory;
import com.stanfy.helium.entities.json.JsonEntityWriter;
import com.stanfy.helium.model.HttpHeader;
import com.stanfy.helium.model.MethodType;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.ServiceMethod;
import com.stanfy.helium.model.TypeResolver;
import com.stanfy.helium.model.tests.MethodTestInfo;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements ScenarioExecutor using HTTP client.
 */
class HttpExecutor implements ScenarioExecutor {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(HttpExecutor.class);

  /** Default encoding. */
  private static final String DEFAULT_ENCODING = "UTF-8";

  /** Type resolver. */
  private final TypeResolver types;

  public HttpExecutor(final TypeResolver resolver) {
    this.types = resolver;
  }

  public static String resolveEncoding(final Service service, final ServiceMethod method) {
    String encoding = method.getEncoding();
    if (encoding == null) {
      encoding = service.getEncoding();
    }
    if (encoding == null) {
      encoding = DEFAULT_ENCODING;
    }
    return encoding;
  }

  public static HttpRequestBase createRequest(final MethodType type) {
    switch (type) {
      case GET: return new HttpGet();
      case POST: return new HttpPost();
      case PATCH: return new HttpPatch();
      case DELETE: return new HttpDelete();
      case PUT: return new HttpPut();
    default:
      throw new UnsupportedOperationException("Unsupported type " + type);
    }
  }

  public static HttpClientBuilder createHttpClientBuilder() {
    return HttpClientBuilder.create().setUserAgent("Helium");
  }

  public static HttpResponse send(final HttpClient client, final HttpRequestBase request) throws IOException {
    LOG.info("Send request " + request.getRequestLine());
    long startTime = System.currentTimeMillis();
    HttpResponse resp = client.execute(request);
    LOG.info("Response loaded in " + (System.currentTimeMillis() - startTime) + " ms: " + resp.getStatusLine());
    return resp;
  }

  @Override
  public HttpResponseWrapper performMethod(final Service service, final ServiceMethod method, final ServiceMethodRequestValues request) {
    // merge service and test info
    MethodTestInfo testInfo = method.getTestInfo().resolve(service.getTestInfo());
    String encoding = resolveEncoding(service, method);

    // prepare HTTP headers
    HashMap<String, String> httpHeaders = new HashMap<String, String>();
    httpHeaders.putAll(testInfo.getHttpHeaders());
    httpHeaders.putAll(request.getHttpHeaders());
    List<String> unresolvedHeaders = Utils.findUnresolvedHeaders(method, httpHeaders);
    if (!unresolvedHeaders.isEmpty()) {
      throw new IllegalArgumentException("Unresolved headers: " + unresolvedHeaders);
    }
    Utils.checkConstantHeaders(method, httpHeaders);
    for (HttpHeader header : method.getHttpHeaders()) {
      if (header.isConstant()) {
        httpHeaders.put(header.getName(), header.getValue());
      }
    }

    // prepare request URI
    String requestPath = service.getMethodUri(method, request.getPathParameters());
    String query = "";
    if (request.getParameters() != null) {
      StringWriter queryWriter = new StringWriter();
      try {
        new HttpParamsWriter(queryWriter, encoding).write(request.getParameters());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      query = queryWriter.toString();
      if (query.length() > 0) { query = "?" + query; }
    }
    String requestUri = requestPath + query;

    HttpRequestBase httpRequest = createRequest(method.getType());

    // set URI
    try {
      httpRequest.setURI(new URI(requestUri));
    } catch (URISyntaxException e) {
      throw new RuntimeException("Bad URI generated for method " + method, e);
    }

    // set headers
    for (Map.Entry<String, String> pair : httpHeaders.entrySet()) {
      if (pair.getValue() != null && pair.getValue().length() > 0) {
        httpRequest.addHeader(pair.getKey(), pair.getValue());
      }
    }

    // set body
    // TODO: support different content types
    if (method.getType().isHasBody() && method.getBody() != null) {
      StringWriter json = new StringWriter();
      try {
        new JsonEntityWriter(json, types.<JsonReader, JsonWriter>findConverters(JsonConverterFactory.JSON)).write(request.getBody());
      } catch (IOException e) {
        throw new RuntimeException("Cannot serialize request body", e);
      }
      String body = json.toString();
      LOG.debug("Body: " + body);
      try {
        ((HttpEntityEnclosingRequestBase)httpRequest).setEntity(new StringEntity(body, encoding));
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("Encoding " + encoding + " is not supported", e);
      }
    }

    HttpClient client = createHttpClientBuilder().build();
    try {
      return new HttpResponseWrapper(types, httpRequest, send(client, httpRequest), encoding, method.getResponse());
    } catch (IOException e) {
      throw new RuntimeException("Cannot execute HTTP request", e);
    }

  }

}
