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

  final Subtype subtype;

  MultipartType(final String subtype) {
    if (!isSubtypeAllowed(subtype)) {
      throw new IllegalArgumentException("Bad content type of multipart body: $subtype")
    }
    this.@subtype = Subtype.from(subtype)
  }

  MultipartType() {
    this.@subtype = Subtype.MIXED
  }

  static boolean isSubtypeAllowed(final String type) {
    def types = Subtype.values().collect { Subtype ct ->
      ct.representation()
    }
    return types.contains(type)
  }

  boolean isGeneric() {
    return parts?.isEmpty()
  }

  /**
   * Enum that represents subtype of multipart.
   * For more info see <a href="http://www.w3.org/Protocols/rfc1341/7_2_Multipart.html">w3 spec</a>.
   */
  static enum Subtype {

    MIXED,
    ALTERNATIVE,
    DIGEST,
    PARALLEL,
    FORM_DATA{
      @Override
      String representation() {
        return 'form-data'
      }
    }

    public static Subtype from(final String str) {
      if ('form-data'.equalsIgnoreCase(str)) {
        return FORM_DATA
      }
      return valueOf(str.toUpperCase(Locale.US))
    }

    public String representation() {
      this.name().toLowerCase()
    }
  }
}
