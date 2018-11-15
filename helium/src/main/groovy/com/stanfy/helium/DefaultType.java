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

  public static boolean isTypeCustom(Type type) {
    if (type.isAnonymous()) {
      return false;
    }
    if (!type.isPrimitive()) {
      return false;
    }
    for (DefaultType dt : DefaultType.values()) {
      if (dt.getLangName().equals(type.getName())) {
        return false;
      }
    }
    return true;
  }

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
