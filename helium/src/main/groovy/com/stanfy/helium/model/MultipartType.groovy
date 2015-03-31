package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Represents <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2">http multipart form</a>.
 *
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
@CompileStatic
class MultipartType extends Type {

  /**
   * Parts are returned in order of adding.
   */
  final Map<String, Type> parts = new LinkedHashMap<>();

  final ContentType type;

  MultipartType(final String type) {
    if (!isContentTypeAllowed(type)) {
      throw new IllegalArgumentException("Bad content type of multipart body: $type")
    }
    this.@type = ContentType.valueOf(type.toUpperCase(Locale.US))
  }

  MultipartType() {
    this.@type = ContentType.MIXED
  }

  static boolean isContentTypeAllowed(final String type) {
    def types = ContentType.values().collect { ContentType ct ->
      ct.representation()
    }
    return types.contains(type)
  }

  boolean isGeneric() {
    return parts?.isEmpty()
  }

  static enum ContentType {
    MIXED,
    ALTERNATIVE,
    DIGEST,
    PARALLEL,
    FORM

    public String representation() {
      this.name().toLowerCase()
    }
  }
}
