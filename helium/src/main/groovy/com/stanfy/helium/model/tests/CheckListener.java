package com.stanfy.helium.model.tests;

public interface CheckListener {

  void onSuiteStarted(BehaviourSuite suite);

  void onCheckStarted(BehaviourCheck check);

  void onCheckDone(BehaviourCheck check);

  void onSuiteDone(BehaviourSuite suite);

}
