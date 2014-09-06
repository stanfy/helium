package com.stanfy.helium.handler.codegen.json.schema;

import java.util.Locale;

/**
 * JSON schema types.
 *
 * @author Michael Pustovit mpustovit@stanfy.com.ua
 */
enum JsonType {
  ARRAY,
  BOOLEAN,
  INTEGER,
  NUMBER,
  NULL,
  OBJECT,
  STRING;

  public String getName() {
    return this.name().toLowerCase(Locale.US);
  }
}
