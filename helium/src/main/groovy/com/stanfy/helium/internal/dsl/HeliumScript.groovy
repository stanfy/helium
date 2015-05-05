package com.stanfy.helium.internal.dsl

/**
 * Spec script base type.
 */
abstract class HeliumScript extends Script {

  /** DSL instance. */
  private ProjectDsl project

  public void setProject(final ProjectDsl dsl) {
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
