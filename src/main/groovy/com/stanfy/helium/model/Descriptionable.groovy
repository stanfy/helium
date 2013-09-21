package com.stanfy.helium.model

/**
 * Entity that has name and description.
 */
class Descriptionable extends ConfigurableEntity {

  /** Name. */
  String name

  /** Description. */
  String description;

  @Override
  String toString() {
    return "${getClass().simpleName}($name)"
  }
}
