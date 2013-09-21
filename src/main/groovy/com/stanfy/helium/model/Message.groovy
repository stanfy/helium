package com.stanfy.helium.model

import groovy.transform.ToString

/**
 * A message.
 */
@ToString
class Message {

  /** Message name. */
  String name

  /** Message fields. */
  final List<Field> fields = new ArrayList<>()

}
