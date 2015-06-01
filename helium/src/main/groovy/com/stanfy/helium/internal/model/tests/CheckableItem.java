package com.stanfy.helium.internal.model.tests;

import com.stanfy.helium.internal.MethodsExecutor;
import com.stanfy.helium.model.tests.BehaviourCheck;
import com.stanfy.helium.model.tests.CheckListener;

public interface CheckableItem {
  <T extends BehaviourCheck> T check(MethodsExecutor executor, CheckListener listener);
}
