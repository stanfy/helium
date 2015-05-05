package com.stanfy.helium.model;

import com.stanfy.helium.model.tests.BehaviourSuite;

/**
 * Interface for entities that allow to check their behaviour specification.
 * Check result is a {@link BehaviourSuite}.
 */
public interface Checkable {

  BehaviourSuite check();

}
