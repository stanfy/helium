package com.stanfy.helium.internal.utils;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.stanfy.helium.handler.tests.BadHttpResponseException;
import com.stanfy.helium.internal.entities.TypedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.net.HttpURLConnection.*;

/**
 * Assertion utils.
 */
public final class AssertionUtils {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(AssertionUtils.class);

  private AssertionUtils() { /* hidden */ }

  public static void notNull(final String name, final Object arg) {
    if (arg == null) {
      throw new IllegalArgumentException(name + " cannot be null");
    }
  }

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
    return new BadHttpResponseException(message, request, response);
  }

}
