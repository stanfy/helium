package com.stanfy.helium.model

import groovy.transform.CompileStatic

import java.util.regex.Pattern

/**
 * Message field.
 */
@CompileStatic
class Field extends Descriptionable {

  /** Name pattern. */
  private static final Pattern NAME_PATTERN = ~/^[a-zA-Z0-9_-]+$/

  /** Field type. */
  Type type

  /** Required option, true by default. */
  boolean required = true

  /** Sequence option. */
  boolean sequence

  /** Value examples. */
  private List<String> examples

  /** Marks this field as ignorable. */
  boolean skip

  @Override
  void setName(final String name) {
    if (!NAME_PATTERN.matcher(name).matches()) {
      throw new IllegalArgumentException("Name must match ${NAME_PATTERN.pattern()}")
    }
    super.setName(name)
  }

  void setExamples(List<String> examples) {
    if (type instanceof Message) {
      throw new IllegalStateException("Examples can be provided for primitives only")
    }
    this.@examples = examples
  }

  List<String> getExamples() {
    return this.@examples ? Collections.unmodifiableList(this.@examples) : Collections.emptyList()
  }

  boolean isRequired() {
    return this.@required && !this.@skip
  }

}
