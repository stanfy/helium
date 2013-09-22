package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Message field.
 */
@CompileStatic
class Field extends Descriptionable {

  /** Field type. */
  Type type

  /** Required option, true by default. */
  boolean required = true

}
