package com.stanfy.helium.internal.model.tests;

import com.stanfy.helium.model.tests.BehaviourCheck;

public interface CheckableItem {
  <T extends BehaviourCheck> T check();
}
