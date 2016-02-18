package com.stanfy.helium;

import com.stanfy.helium.model.Type;

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

  private Type type;

  synchronized void setType(Type type) {
    this.type = type;
  }

  public synchronized Type getType() {
    if (type == null) {
      type = new Type();
      type.setName(getLangName());
    }
    return type;
  }

  public String getLangName() {
    return this.name().toLowerCase(Locale.US);
  }

}
