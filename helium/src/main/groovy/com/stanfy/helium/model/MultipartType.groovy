package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * Represents <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2">http multipart form</a>.
 *
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
@CompileStatic
class MultipartType extends Type {

  final Map<String, Type> parts = new HashMap<>();

  MultipartType(final Message message) {
    if (!message) {
      return
    }
    for (Field f in message.activeFields) {
      parts.put(f.name, f.type)
    }
  }

  MultipartType() {
  }

  boolean isGeneric() {
    return parts?.isEmpty()
  }
}
