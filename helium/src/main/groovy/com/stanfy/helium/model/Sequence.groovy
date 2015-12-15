package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Sequence of something.
 */
@CompileStatic
final class Sequence extends Type {

  /** Items type. */
  Type itemsType

  boolean isPrimitive() { return false }

}
