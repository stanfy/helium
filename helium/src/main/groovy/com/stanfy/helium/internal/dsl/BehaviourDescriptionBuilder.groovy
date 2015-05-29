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
  private final ProjectDsl project

  BehaviourDescriptionBuilder(final String name, final BehaviorDescriptionContainer target, final ProjectDsl project) {
    this.name = name
    this.target = target
    this.project = project
  }

  void spec(Closure<Void> action) {
    target.addBehaviourDescription(new BehaviourDescription(name: name, action: action, project: project))
  }

}
