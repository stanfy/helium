package com.stanfy.helium;

import java.util.Locale;

/**
 * Default type.
 */
public enum DefaultType {

  DOUBLE,
  FLOAT,
  INT32,
  INT64,
  BOOL,
  STRING,
  BYTES;

  public String getLangName() {
    return this.name().toLowerCase(Locale.US);
  }

}
