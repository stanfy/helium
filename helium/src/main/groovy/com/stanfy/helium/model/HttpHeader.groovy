package com.stanfy.helium.model

/**
 * HTTP header representation.
 */
class HttpHeader {

  /** Name. */
  String name

  /** Constant value. */
  String value

  /** Value examples. */
  List<String> examples

  boolean isConstant() {
    return value != null && value.length() > 0
  }

}
