package com.stanfy.helium.model;

import com.stanfy.helium.internal.MethodsExecutor;
import com.stanfy.helium.internal.model.tests.CheckableItem;
import com.stanfy.helium.model.tests.BehaviourSuite;
import com.stanfy.helium.model.tests.CheckListener;

/**
 * Interface for entities that allow to check their behaviour specification.
 * Check result is a {@link BehaviourSuite}.
 */
public interface Checkable extends CheckableItem {

  @SuppressWarnings("unchecked")
  BehaviourSuite check(MethodsExecutor executor, CheckListener listener);

}
