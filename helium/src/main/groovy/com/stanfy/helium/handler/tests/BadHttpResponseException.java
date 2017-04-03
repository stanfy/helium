package com.stanfy.helium.handler.tests;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import okio.Buffer;

import java.io.IOException;

/** Thrown when unexpected HTTP response is received. */
public class BadHttpResponseException extends AssertionError {

  private final Request request;
  private final Response response;

  public BadHttpResponseException(Request request, Response response) {
    super(message(request, response));
    this.request = request;
    this.response = response;
  }

  public BadHttpResponseException(String message, Request request, Response response) {
    super(message + "\n\n" + message(request, response));
    this.request = request;
    this.response = response;
  }

  private static String message(Request request, Response response) {
    return "------------- HTTP details ------------\n"
        + httpDump(request, response) + "\n\n"
        + "curl cmd: \n"
        + curl(request);
  }

  private static String escape(String str) {
    return str.replace("'", "\\'");
  }

  public Request getRequest() {
    return request;
  }

  public Response getResponse() {
    return response;
  }

  private static String curl(Request request) {
    StringBuilder result = new StringBuilder();
    result.append("curl -X ").append(request.method());
    for (String header : request.headers().names()) {
      for (String value : request.headers().values(header)) {
        result.append(" -H '").append(escape(header)).append(": ").append(escape(value)).append("'");
      }
    }
    if (request.body() != null) {
      result.append(" -d '").append(escape(requestBodyToString(request.body()))).append("'");
    }
    result.append(" '").append(escape(request.urlString())).append("'");
    return result.toString();
  }

  public String curl() {
    return curl(request);
  }

  private static String httpDump(final Request request, final Response response) {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("\nRequest info: ");

    stringBuilder.append(request.method())
        .append(' ')
        .append(request.urlString())
        .append('\n');

    dumpHeaders(request.headers(), stringBuilder);

    if (request.body() != null) {
      final String loggedEntity = requestBodyToString(request.body());
      stringBuilder.append('\n').append(loggedEntity).append('\n');
    }

    stringBuilder.append('\n');
    if (response == null) {
      stringBuilder.append("Response info is not available.");
    } else {
      stringBuilder.append("Response info:\n")
          .append(response.code()).append(" ").append(response.message()).append("\n");

      dumpHeaders(response.headers(), stringBuilder);

      stringBuilder
          .append('\n')
          .append(responseBodyToString(response.body())).append('\n');
    }

    return stringBuilder.toString();
  }

  private static void dumpHeaders(final Headers headers, final StringBuilder result) {
    result.append("Headers:\n").append(headers);
  }

  private static String requestBodyToString(final RequestBody body) {
    Buffer buffer = new Buffer();
    try {
      body.writeTo(buffer);
    } catch (IOException e) {
      return "<Cannot get request body: " + e.getMessage() + ">";
    }
    return buffer.readUtf8();
  }

  private static String responseBodyToString(final ResponseBody body) {
    try {
      return body.source().readUtf8();
    } catch (IOException e) {
      return "<Cannot get response body: " + e.getMessage() + ">";
    }
  }
}
