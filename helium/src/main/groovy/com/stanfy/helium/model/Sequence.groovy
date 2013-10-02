package com.stanfy.helium.model

/**
 * Sequence of something.
 */
class Sequence extends Type {

  /** Items type. */
  Type itemsType

  boolean isPrimitive() { return false }

}
