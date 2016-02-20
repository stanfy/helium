package com.stanfy.helium.model

/** Security specification. */
class Security extends Descriptionable {

  Type type

  /** Type of security scheme. */
  enum Type {
    CERTIFICATE,
    BASIC,
    API_KEY,
    OAUTH

    String getName() {
      return name().toLowerCase(Locale.US)
    }
  }

}
