package com.stanfy.helium.handler.tests;

import com.stanfy.helium.model.tests.BehaviourCheck;
import com.stanfy.helium.model.tests.BehaviourSuite;
import com.stanfy.helium.model.tests.CheckListener;

import java.util.Collection;

class ComposedCheckListener implements CheckListener, HeliumTestLog {
  private final Collection<CheckListener> listeners;

  ComposedCheckListener(final Collection<CheckListener> listeners) {
    this.listeners = listeners;
  }


  @Override
  public void onSuiteStarted(final BehaviourSuite suite) {
    for (CheckListener l : listeners) {
      l.onSuiteStarted(suite);
    }
  }

  @Override
  public void onCheckStarted(final BehaviourCheck check) {
    for (CheckListener l : listeners) {
      l.onCheckStarted(check);
    }
  }

  @Override
  public void onCheckDone(final BehaviourCheck check) {
    for (CheckListener l : listeners) {
      l.onCheckDone(check);
    }
  }

  @Override
  public void onSuiteDone(final BehaviourSuite suite) {
    for (CheckListener l : listeners) {
      l.onSuiteDone(suite);
    }
  }

  @Override
  public void write(final String fmt, final Object... args) {
    for (CheckListener l : listeners) {
      if (l instanceof HeliumTestLog) {
        ((HeliumTestLog) l).write(fmt, args);
      }
    }
  }

}
