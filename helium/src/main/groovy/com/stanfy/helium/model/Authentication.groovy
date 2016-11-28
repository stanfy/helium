package com.stanfy.helium.model

/** Authentication specification. */
class Authentication extends Descriptionable {

  Type type

  /** Type of authentication scheme. */
  enum Type {
    CERTIFICATE,
    BASIC,
    API_KEY,
    OAUTH2

    String getName() {
      return name().toLowerCase(Locale.US)
    }
  }

}
