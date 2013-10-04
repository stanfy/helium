package com.stanfy.helium.utils;

import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.entities.ValidationError;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Assertion utils.
 */
public final class AssertionUtils {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(AssertionUtils.class);

  private AssertionUtils() { /* hidden */ }

  public static void validateStatus(final HttpResponse response, final boolean success) {
    String status = response.getStatusLine().getReasonPhrase();
    String reason = status != null && status.length() > 0 ? " Got '" + status + "'" : "";

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
    return "TODO";
  }

  public static void assertCorrectEntity(final TypedEntity entity, final HttpRequest request) {
    List<ValidationError> errors = entity.getValidationErrors();
    assertThat(errors).describedAs("Validation errors are present. Request info: " + getRequestInfo(request)).isEmpty();
  }

}
