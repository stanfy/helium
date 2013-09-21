package com.stanfy.helium.model

/**
 * Entity that has name and description.
 */
class Descriptionable {

  /** Name. */
  String name

  /** Description. */
  String description;

  @Override
  String toString() {
    return "${getClass().simpleName}($name)"
  }
}
