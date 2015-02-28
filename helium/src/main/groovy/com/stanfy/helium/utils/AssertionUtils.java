package com.stanfy.helium.utils;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.stanfy.helium.entities.TypedEntity;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.net.HttpURLConnection.*;

/**
 * Assertion utils.
 */
public final class AssertionUtils {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(AssertionUtils.class);

  private AssertionUtils() { /* hidden */ }

  public static void validateStatus(final Response response, final boolean success) {
    Request request = response.request();
    int statusCode = response.code();

    if (statusCode == HTTP_NOT_FOUND) {
      throw failure("Method not found... Maybe not implemented yet?", request, response);
    }

    if (success) {
      LOG.info("Validating successful status...");

      if (statusCode == HTTP_BAD_METHOD) {
        throw failure("HTTP method is not allowed", request, response);
      }

      if (!response.isSuccessful()) {
        throw failure("Successful HTTP status code expected.", request, response);
      }

    } else {
      LOG.info("Validating error status...");

      if (statusCode < HTTP_BAD_REQUEST || statusCode >= HTTP_INTERNAL_ERROR) {
        throw failure("Client error expected.", request, response);
      }
    }
  }

  public static void assertCorrectEntity(final TypedEntity entity, final Response response) {
    if (entity.getValidationError() != null) {
      Request request = response.request();
      throw failure(
          request.method() + " " + request.urlString() + "\n"
              + "--------- Validation problems ---------\n"
              + entity.getValidationError(),
          request, response
      );
    }
  }

  private static AssertionError failure(final String message, final Request request, final Response response) {
    return  new AssertionError("\n\n"
        + message + "\n\n"
        + "------------- HTTP details ------------\n"
        + getRequestInfo(request, response) + "\n\n"
    );
  }

  private static String getRequestInfo(final Request request, final Response response) {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("\nRequest info: ");

    stringBuilder.append(request.method())
        .append(' ')
        .append(request.urlString())
        .append('\n');

    dumpHeaders(request.headers(), stringBuilder);

    if (request.body() != null) {
      final String loggedEntity = requestBodyToString(request.body());
      if (loggedEntity != null) {
        stringBuilder.append('\n').append(loggedEntity).append('\n');
      }
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
    result.append("Headers:\n");
    for (String hName : headers.names()) {
      for (String hValue : headers.values(hName)) {
        result.append(hName).append(':').append(hValue).append('\n');
      }
    }
  }

  private static String requestBodyToString(final RequestBody body) {
    Buffer buffer = new Buffer();
    try {
      body.writeTo(buffer);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return buffer.readUtf8();
  }

  private static String responseBodyToString(final ResponseBody body) {
    try {
      return body.source().readUtf8();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

}
