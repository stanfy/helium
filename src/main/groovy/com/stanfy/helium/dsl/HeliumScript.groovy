package com.stanfy.helium.dsl

/**
 * Spec script base type.
 */
abstract class HeliumScript extends Script {

  /** DSL instance. */
  private Project project

  public void setProject(final Project dsl) {
    this.project = dsl
  }

  @Override
  def invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args)
    } catch (MissingMethodException e) {
      if (name == e.method) {
        project.invokeMethod(name, args)
      } else {
        throw e
      }
    }
  }

}
