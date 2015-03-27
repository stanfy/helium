package com.stanfy.helium.handler.codegen.tests;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.stanfy.helium.dsl.scenario.ScenarioExecutor;
import com.stanfy.helium.dsl.scenario.ServiceMethodRequestValues;
import com.stanfy.helium.entities.ByteArrayEntity;
import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.entities.json.JsonConvertersPool;
import com.stanfy.helium.entities.json.JsonEntityWriter;
import com.stanfy.helium.model.DataType;
import com.stanfy.helium.model.FormType;
import com.stanfy.helium.model.HttpHeader;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.ServiceMethod;
import com.stanfy.helium.model.TypeResolver;
import com.stanfy.helium.model.tests.MethodTestInfo;
import okio.BufferedSink;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements ScenarioExecutor using HTTP client.
 */
class HttpExecutor implements ScenarioExecutor {

  /** Default encoding. */
  private static final String DEFAULT_ENCODING = "UTF-8";

  /** Type resolver. */
  private final TypeResolver types;

  /** HTTP client. */
  private final OkHttpClient client;

  HttpExecutor(final TypeResolver resolver, final OkHttpClient client) {
    this.types = resolver;
    this.client = client;
  }

  static String resolveEncoding(final Service service, final ServiceMethod method) {
    String encoding = method.getEncoding();
    if (encoding == null) {
      encoding = service.getEncoding();
    }
    if (encoding == null) {
      encoding = DEFAULT_ENCODING;
    }
    return encoding;
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

    RequestBody body = null;
    if (method.getType().isHasBody()) {
      body = getRequestBody(method, request, encoding);
    }

    Request httpRequest = new Request.Builder()
        .headers(prepareHeaders(testInfo, method, request))
        .url(resolveUri(service, method, request, encoding))
        .method(method.getType().toString(), body)
        .build();

    try {
      Response response = client.newCall(httpRequest).execute();
      return new HttpResponseWrapper(types, response, method.getResponse());
    } catch (IOException e) {
      throw new AssertionError("Cannot execute HTTP request", e);
    }
  }

  private RequestBody getRequestBody(final ServiceMethod method, final ServiceMethodRequestValues request, final String encoding) {
    RequestBody body;
    final TypedEntity requestBody = request.getBody();
    if (method.getBody() instanceof FormType) {
      FormEncodingBuilder formBuilder = new FormEncodingBuilder();
      final Map<String, Object> map = (Map<String, Object>) requestBody.getValue();
      for (String key : map.keySet()) {
        formBuilder.add(key, String.valueOf(map.get(key)));
      }
      body = formBuilder.build();
    } else if (method.getBody() instanceof DataType) {
      byte[] arr;
      if (requestBody.getValue() instanceof byte[]) {
        arr = (byte[]) requestBody.getValue();
      } else if (requestBody.getValue() instanceof ByteArrayEntity) {
        arr = ((ByteArrayEntity) requestBody.getValue()).getBytes();
      } else {
        throw new IllegalArgumentException("Type " + requestBody.getValue().getClass() + " is not supported for raw data input.");
      }

      body = RequestBody.create(MediaType.parse("application/octet-stream"), arr);

      // TODO: Support also multipart
    } else {
      body = new RequestBody() {
        @Override
        public MediaType contentType() {
          return MediaType.parse("application/json");
        }

        @Override
        public void writeTo(final BufferedSink sink) throws IOException {
          TypedEntity entity = requestBody;
          if (entity != null) {
            Writer out = new OutputStreamWriter(sink.outputStream(), encoding);
            new JsonEntityWriter(out, types.<JsonReader, JsonWriter>findConverters(JsonConvertersPool.JSON)).write(entity);
            out.close();
          }
          sink.close();
        }
      };
    }
    return body;
  }

}
