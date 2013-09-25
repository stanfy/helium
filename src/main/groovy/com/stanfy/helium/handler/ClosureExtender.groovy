package com.stanfy.helium.handler

import com.stanfy.helium.model.Project

/**
 * Extends project with closure.
 */
class ClosureExtender implements Handler {

  /** Closure. */
  private final Closure<?> closure

  public ClosureExtender(final Closure<?> closure) {
    this.closure = closure.clone() as Closure<?>
  }

  @Override
  public void handle(final Project project) {
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.delegate = project
    closure.call()
  }

}
