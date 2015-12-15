package com.stanfy.helium.model

import groovy.transform.CompileStatic

/**
 * A message.
 */
@CompileStatic
final class Message extends Type {

  /** Flag that allows to skip (ignore) all unknown fields met in response during validation in generated tests. */
  boolean skipUnknownFields

  Message parent;

  /** Message fields. */
  private final List<Field> fields = new ArrayList<>()

  List<Field> getFields() { return Collections.unmodifiableList(fields) }

  List<Field> getActiveFields() {
    List<Field> active = (List<Field>) fields.findAll() { Field f -> !f.skip }
    return Collections.unmodifiableList(active)
  }

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

  boolean hasRequiredFields() {
    return fields.any { Field f -> f.required }
  }

  boolean isPrimitive() { return false }


  /**
   * Return true if parent is not null or empty.
   * @return true if parent is not null or empty.
   */
  boolean hasParent() {
    return parent != null
  }
}
