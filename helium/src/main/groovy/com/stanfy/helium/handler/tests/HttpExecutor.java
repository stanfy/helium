package com.stanfy.helium.handler.tests;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.stanfy.helium.handler.tests.body.BuilderFactory;
import com.stanfy.helium.internal.MethodsExecutor;
import com.stanfy.helium.internal.ServiceMethodRequestValues;
import com.stanfy.helium.model.HttpHeader;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.ServiceMethod;
import com.stanfy.helium.model.TypeResolver;
import com.stanfy.helium.model.tests.MethodTestInfo;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stanfy.helium.handler.tests.Utils.resolveEncoding;

/**
 * Implements ScenarioExecutor using HTTP client.
 */
public class HttpExecutor implements MethodsExecutor {

  /** Type resolver. */
  private final TypeResolver types;

  /** HTTP client. */
  private final OkHttpClient client;

  HttpExecutor(final TypeResolver resolver, final OkHttpClient client) {
    this.types = resolver;
    this.client = client;
  }

  private static Headers prepareHeaders(final MethodTestInfo testInfo, final ServiceMethod method,
                                        final ServiceMethodRequestValues request) {
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
    Headers.Builder builder = new Headers.Builder();
    for (Map.Entry<String, String> h : httpHeaders.entrySet()) {
      builder.add(h.getKey(), h.getValue());
    }
    return builder.build();
  }

  private static String resolveUri(final Service service, final ServiceMethod method,
                                   final ServiceMethodRequestValues request, final String encoding) {
    String requestPath = service.getMethodUri(method, request.getPathParameters());
    String query = "";
    if (request.getParameters() != null) {
      StringWriter queryWriter = new StringWriter();
      try {
        new HttpParamsWriter(queryWriter, encoding).write(request.getParameters());
      } catch (IOException e) {
        throw new AssertionError(e);
      }
      query = queryWriter.toString();
      if (query.length() > 0) {
        query = "?" + query;
      }
    }
    return requestPath + query;
  }

  @Override
  public HttpResponseWrapper performMethod(final Service service, final ServiceMethod method, final ServiceMethodRequestValues request) {
    // merge service and test info
    MethodTestInfo testInfo = method.getTestInfo().resolve(service.getTestInfo());
    final String encoding = resolveEncoding(service, method);

    Headers headers = prepareHeaders(testInfo, method, request);

    RequestBody body = null;
    if (method.getType().isHasBody()) {
      body = getRequestBody(method, request, encoding, headers);
    }

    Request httpRequest = new Request.Builder()
        .headers(headers)
        .url(resolveUri(service, method, request, encoding))
        .method(method.getType().toString(), body)
        .build();

    try {
      OkHttpClient client = this.client;
      if (testInfo.getAuthParams() != null) {
        Interceptor authInterceptor = testInfo.getAuthParams().httpInterceptor();
        if (client.interceptors().indexOf(authInterceptor) == -1) {
          client.interceptors().add(0, authInterceptor);
        }
      }
      Response response = client.newCall(httpRequest).execute();
      return new HttpResponseWrapper(types, response, method.getResponse());
    } catch (IOException e) {
      throw new IllegalStateException("Cannot execute HTTP request: " + e.getMessage(), e);
    }
  }

  private RequestBody getRequestBody(final ServiceMethod method, final ServiceMethodRequestValues request,
                                     final String encoding, final Headers headers) {
    final RequestBodyBuilder builder = BuilderFactory.getBuilderFor(method.getBody());
    MediaType contentType;
    String headerContentType = headers.get("Content-Type");
    if (headerContentType != null) {
      contentType = MediaType.parse(headerContentType);
    } else {
      contentType = Utils.jsonType();
    }
    return builder.build(types, request.getBody(), contentType, encoding);
  }

}
