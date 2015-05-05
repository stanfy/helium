package com.stanfy.helium.internal.dsl

import com.stanfy.helium.internal.model.tests.BehaviorDescriptionContainer
import com.stanfy.helium.internal.model.tests.BehaviourDescription
import groovy.transform.CompileStatic

/**
 * Builds a new behaviour description adding it to a service or project.
 */
@CompileStatic
class BehaviourDescriptionBuilder {

  private final String name
  private final BehaviorDescriptionContainer target

  BehaviourDescriptionBuilder(final String name, final BehaviorDescriptionContainer target) {
    this.name = name
    this.target = target
  }

  void spec(Closure<Void> action) {
    target.addBehaviourDescription(new BehaviourDescription(name: name, action: action))
  }

}
