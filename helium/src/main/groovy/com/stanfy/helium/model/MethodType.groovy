package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Method type.
 */
@CompileStatic
enum MethodType {

  GET(false),
  POST(true),
  PUT(true),
  DELETE(false),
  PATCH(true)

  /** Whether request has body. */
  final boolean hasBody

  private MethodType(final boolean hasBody) {
    this.hasBody = hasBody
  }

  public String getName() {
    return toString().toLowerCase(Locale.US)
  }

}
