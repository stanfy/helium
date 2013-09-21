package com.stanfy.helium.dsl

/**
 * Spec script base type.
 */
abstract class HeliumScript extends Script {

  /** DSL instance. */
  private Dsl dsl

  public void setDsl(final Dsl dsl) {
    this.dsl = dsl
  }

  @Override
  def invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args)
    } catch (MissingMethodException e) {
      if (name == e.method) {
        dsl.invokeMethod(name, args)
      } else {
        throw e
      }
    }
  }

}
