package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * General note.
 */
@CompileStatic
class Note implements StructureUnit {

  /** String value */
  String value

  @Override
  String toString() {
    String part = value.length() > 10 ? value[0..10] : value
    return "Note($part)"
  }

}
