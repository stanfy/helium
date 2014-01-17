package com.stanfy.helium.utils;

import com.stanfy.helium.entities.TypedEntity;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Assertion utils.
 */
public final class AssertionUtils {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(AssertionUtils.class);

  private AssertionUtils() { /* hidden */ }

  public static void validateStatus(final HttpRequest request, final HttpResponse response, final boolean success) {
    int statusCode = response.getStatusLine().getStatusCode();

    if (statusCode == HttpStatus.SC_NOT_FOUND) {
      throw failure("Method not found... Maybe not implemented yet?", request, response);
    }

    if (success) {
      LOG.info("Validating successful status...");

      if (statusCode == HttpStatus.SC_METHOD_NOT_ALLOWED) {
        throw failure("HTTP method is not allowed", request, response);
      }

      if (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
        throw failure("Successful HTTP status code expected.", request, response);
      }

    } else {
      LOG.info("Validating error status...");

      if (statusCode < HttpStatus.SC_BAD_REQUEST || statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
        throw failure("Client error expected.", request, response);
      }
    }
  }

  public static void assertCorrectEntity(final TypedEntity entity, final HttpRequest request,
                                         final HttpResponse response) {
    if (entity.getValidationError() != null) {
      throw failure(
          "--------- Validation problems ---------\n" + entity.getValidationError(),
          request, response
      );
    }
  }

  private static AssertionError failure(final String message, final HttpRequest request, final HttpResponse response) {
    return  new AssertionError("\n\n"
        + message + "\n\n"
        + "------------- HTTP details ------------\n"
        + getRequestInfo(request, response) + "\n\n"
    );
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
      StatusLine statusLine = response.getStatusLine();
      final String status = statusLine.getReasonPhrase();
      if (status != null && status.length() > 0) {
        stringBuilder.append("Got '").append(status).append("', ").append(statusLine.getStatusCode()).append(".\n");
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
