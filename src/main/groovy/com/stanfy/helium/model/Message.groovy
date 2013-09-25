package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * A message.
 */
@CompileStatic
class Message extends Type {

  /** Message fields. */
  private final List<Field> fields = new ArrayList<>()

  List<Field> getFields() { return Collections.unmodifiableList(fields) }

  Field fieldByName(final String name) {
    return fields.find { Field field -> field.name == name }
  }

  void addField(final Field f) {
    if (fieldByName(f.name)) {
      throw new IllegalArgumentException("Field with name $f.name is already defined in message $name")
    }
    fields.add f
  }

  Collection<Field> getRequiredFields() {
    return fields.findAll() { Field field -> field.required }
  }

  boolean isArray() {
    // TODO
    return false
  }

}
