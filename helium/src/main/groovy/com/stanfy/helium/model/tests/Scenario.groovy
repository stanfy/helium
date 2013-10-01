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

}
