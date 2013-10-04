package com.stanfy.helium.model.tests

import com.stanfy.helium.model.Descriptionable

/**
 * Test scenario.
 */
class Scenario extends Descriptionable {

  /** Before action. */
  Closure<?> before

  /** After action. */
  Closure<?> after

  /** Scenario main action. */
  Closure<?> action

  String getCanonicalName() {
    String res = name.trim().replaceAll(/\s+/, '_').replaceAll(/\W+/, '')
    return res[-1] == '_' ? res[0..-2] : res
  }


}
