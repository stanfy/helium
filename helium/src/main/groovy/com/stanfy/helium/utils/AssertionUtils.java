package com.stanfy.helium.utils;

import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.entities.ValidationError;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Assertion utils.
 */
public final class AssertionUtils {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(AssertionUtils.class);

  private AssertionUtils() { /* hidden */ }

  public static void validateStatus(final HttpRequest request, final HttpResponse response, final boolean success) {
    final String requestInfo = getRequestInfo(request, response);

    assertThat(response.getStatusLine().getStatusCode())
        .describedAs("Method not found... Maybe not implemented yet? " + requestInfo)
        .isNotEqualTo(HttpStatus.SC_NOT_FOUND);

    assertThat(response.getStatusLine().getStatusCode())
        .describedAs("Incorrect HTTP method. " + requestInfo)
        .isNotEqualTo(HttpStatus.SC_METHOD_NOT_ALLOWED);

    if (success) {
      LOG.info("Validating successful status...");
      assertThat(response.getStatusLine().getStatusCode())
          .describedAs("Successful HTTP status code expected. " + requestInfo)
          .isGreaterThanOrEqualTo(HttpStatus.SC_OK)
          .isLessThan(HttpStatus.SC_MULTIPLE_CHOICES);
    } else {
      LOG.info("Validating error status...");
      assertThat(response.getStatusLine().getStatusCode())
          .describedAs("Client error expected. " + requestInfo)
          .isGreaterThanOrEqualTo(HttpStatus.SC_BAD_REQUEST)
          .isLessThan(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
  }

  public static void assertCorrectEntity(final TypedEntity entity, final HttpRequest request, final HttpResponse response) {
    List<ValidationError> errors = entity.getValidationErrors();
    assertThat(errors).describedAs("Validation errors are present. " + getRequestInfo(request, response)).isEmpty();
  }

  private static String getRequestInfo(final HttpRequest request, final HttpResponse response) {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("\nRequest info: ");

    final RequestLine requestLine = request.getRequestLine();
    stringBuilder.append(requestLine.getMethod())
        .append(' ')
        .append(requestLine.getUri())
        .append('\n')
        .append("Headers:\n");
    for (Header header : request.getAllHeaders()) {
      stringBuilder.append(header).append("\n");
    }

    if (request instanceof HttpEntityEnclosingRequestBase) {
      final HttpEntityEnclosingRequestBase requestWithEntity = (HttpEntityEnclosingRequestBase) request;
      final String loggedEntity = httpEntityToString(requestWithEntity.getEntity());
      if (loggedEntity != null) {
        stringBuilder.append('\n').append(loggedEntity).append('\n');
      }
    }

    stringBuilder.append('\n');
    if (response == null) {
      stringBuilder.append("Response info is not available.");
    } else {
      stringBuilder.append("Response info: ");
      final String status = response.getStatusLine().getReasonPhrase();
      if (status != null && status.length() > 0) {
        stringBuilder.append("Got '").append(status).append("'.\n");
      }

      stringBuilder.append("Headers:\n");
      for (Header header : response.getAllHeaders()) {
        stringBuilder.append(header).append("\n");
      }

      final String loggedEntity = httpEntityToString(response.getEntity());
      if (loggedEntity != null) {
        stringBuilder.append('\n').append(loggedEntity).append('\n');
      }
    }

    return stringBuilder.toString();
  }


  private static String httpEntityToString(final HttpEntity entity) {
    if (entity != null && !entity.isStreaming()) {
      try {
        final InputStream inputStream = entity.getContent();
        if (inputStream != null && inputStream.markSupported()) {
          //assume that InputStream implementation has marked 0 position by default
          inputStream.reset();
          final int kb = 1024;
          final String result;
          if (inputStream.available() > kb * kb) {
            result = "Entity is too big to be logged here: " + inputStream.available() + "bytes";
          } else {
            result = "Entity: " + IOUtils.toString(inputStream);
            inputStream.reset();
          }
          return result;
        }
      } catch (final IOException ioe) {
        //ignore
      }
    }

    return null;
  }

}
