package com.stanfy.helium.handler.tests;

import com.squareup.okhttp.MediaType;
import com.stanfy.helium.model.HttpHeader;
import com.stanfy.helium.model.Service;
import com.stanfy.helium.model.ServiceMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Some tools used for tests generation.
 */
//TODO: Make package scope!
public final class Utils {

  /** Default encoding. */
  private static final String DEFAULT_ENCODING = "UTF-8";

  private Utils() { }

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

  public static List<String> findUnresolvedHeaders(final ServiceMethod method, final Map<String, String> headers) {
    ArrayList<String> result = new ArrayList<String>();
    for (HttpHeader h : method.getHttpHeaders()) {
      if (!h.isConstant() && !headers.containsKey(h.getName())) {
        result.add(h.getName());
      }
    }
    return result;
  }

  static void checkConstantHeaders(final ServiceMethod method, final Map<String, String> userHeaders) {
    for (HttpHeader h : method.getHttpHeaders()) {
      if (h.isConstant() && userHeaders.containsKey(h.getName())) {
        throw new IllegalArgumentException("Trying to override constant header " + h.getName());
      }
    }
  }

  /** Return {@link MediaType} of <strong>application/octet-stream</strong>. */
  public static MediaType bytesType() {
    return MediaType.parse("application/octet-stream");
  }

  /** Return {@link MediaType} of <strong>application/json</strong>. */
  public static MediaType jsonType() {
    return MediaType.parse("application/json");
  }

}
