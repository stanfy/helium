package com.stanfy.helium.utils;

import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.entities.ValidationError;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Assertion utils.
 */
public final class AssertionUtils {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(AssertionUtils.class);

  private AssertionUtils() { /* hidden */ }

  public static void validateStatus(final HttpRequest request, final HttpResponse response, final boolean success) {
    String status = response.getStatusLine().getReasonPhrase();
    String reason = (status != null && status.length() > 0 ? " Got '" + status + "'" : "") + " Request info: " + getRequestInfo(request);

    assertThat(response.getStatusLine().getStatusCode())
        .describedAs("Method not found... Maybe not implemented yet?" + reason)
        .isNotEqualTo(HttpStatus.SC_NOT_FOUND);

    assertThat(response.getStatusLine().getStatusCode())
        .describedAs("Incorrect HTTP method" + reason)
        .isNotEqualTo(HttpStatus.SC_METHOD_NOT_ALLOWED);

    if (success) {
      LOG.info("Validating successful status...");
      assertThat(response.getStatusLine().getStatusCode())
          .describedAs("Successful HTTP status code expected." + reason)
          .isGreaterThanOrEqualTo(HttpStatus.SC_OK)
          .isLessThan(HttpStatus.SC_MULTIPLE_CHOICES);
    } else {
      LOG.info("Validating error status...");
      assertThat(response.getStatusLine().getStatusCode())
          .describedAs("Client error expected." + reason)
          .isGreaterThanOrEqualTo(HttpStatus.SC_BAD_REQUEST)
          .isLessThan(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private static String getRequestInfo(final HttpRequest request) {
    final RequestLine requestLine = request.getRequestLine();
    final StringBuilder sb = new StringBuilder()
        .append(requestLine.getMethod())
        .append(' ')
        .append(requestLine.getUri())
        .append('\n')
        .append("Headers: ").append(Arrays.toString(request.getAllHeaders()));

    if (request instanceof HttpEntityEnclosingRequestBase) {
      final HttpEntityEnclosingRequestBase requestWithEntity = (HttpEntityEnclosingRequestBase) request;
      final HttpEntity entity = requestWithEntity.getEntity();
      if (entity != null && !entity.isStreaming()) {
        try {
          final InputStream inputStream = entity.getContent();
          if (inputStream != null && inputStream.markSupported()) {
            //assume that InputStream implementation has marked 0 position by default
            inputStream.reset();
            final int kb = 1024;
            if (inputStream.available() > kb * kb) {
              sb.append('\n').append("Entity is too big to be logged here: ").append(inputStream.available()).append("bytes");
            } else {
              sb.append('\n').append("Entity: ").append(IOUtils.toString(inputStream));
              inputStream.reset();
            }
          }
        } catch (final IOException ioe) {
          //ignore
        }
      }
    }

    return sb.toString();
  }

  public static void assertCorrectEntity(final TypedEntity entity, final HttpRequest request) {
    List<ValidationError> errors = entity.getValidationErrors();
    assertThat(errors).describedAs("Validation errors are present. Request info: " + getRequestInfo(request)).isEmpty();
  }

}
