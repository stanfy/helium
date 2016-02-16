package com.stanfy.helium.model

/**
 * Key-value collection, a map.
 * Order of entries is not guaranteed.
 */
class Dictionary extends Type {

  /** The key type. */
  Type key

  /** The value type. */
  Type value

  @Override
  boolean isPrimitive() {
    return false
  }

}
