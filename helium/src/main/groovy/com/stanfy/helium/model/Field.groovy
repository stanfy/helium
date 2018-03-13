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

  /** Sequence option. */
  boolean sequence

  /** Value examples. */
  private List<Object> examples

  /** Marks this field as ignorable. */
  boolean skip

  /** Alternative names for deserialization. */
  List<String> alternatives;

  void setExamples(List<Object> examples) {
    if (type instanceof Message) {
      throw new IllegalStateException("Examples can be provided for primitives only")
    }
    this.@examples = examples
  }

  List<Object> getExamples() {
    return this.@examples ? Collections.unmodifiableList(this.@examples) : Collections.emptyList()
  }

  List<String> getAlternatives() {
    return this.@alternatives ? Collections.unmodifiableList(this.@alternatives) : Collections.<String>emptyList()
  }

  boolean isRequired() {
    return this.@required && !this.@skip
  }

}
