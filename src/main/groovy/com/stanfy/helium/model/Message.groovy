package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * A message.
 */
@CompileStatic
class Message extends Type {

  /** Message fields. */
  final List<Field> fields = new ArrayList<>()

}
