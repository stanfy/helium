package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Type of message field or parameter values.
 */
@CompileStatic
class Type extends Descriptionable implements StructureUnit {

  /** True if this type was not defined directly with 'type' declaration. */
  boolean anonymous

  boolean isPrimitive() { return true }

}
