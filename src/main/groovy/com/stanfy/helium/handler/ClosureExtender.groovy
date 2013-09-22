package com.stanfy.helium.handler

import com.stanfy.helium.dsl.Project

/**
 * Extends project with closure.
 */
class ClosureExtender implements Handler {

  /** Closure. */
  private final Closure<?> closure

  public ClosureExtender(final Closure<?> closure) {
    this.closure = closure.clone() as Closure<?>
  }

  public void handle(final Project project) {
    closure.resolveStrategy = Closure.DELEGATE_ONLY
    closure.delegate = project
    closure.call()
  }

}
