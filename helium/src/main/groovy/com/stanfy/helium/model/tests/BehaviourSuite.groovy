package com.stanfy.helium.model.tests

import org.joda.time.Duration

import static com.stanfy.helium.model.tests.BehaviourCheck.Result.PENDING

/**
 * A set of behaviour specification check results.
 */
class BehaviourSuite extends BehaviourCheck {

  public static final BehaviourSuite EMPTY = new BehaviourSuite(
      name: "Empty",
      result: PENDING,
      time: Duration.ZERO
  )

  /** Defined specifications. */
  List<BehaviourCheck> children = Collections.emptyList()

}
