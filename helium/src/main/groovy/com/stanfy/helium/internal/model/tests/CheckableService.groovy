package com.stanfy.helium.internal.model.tests

import com.stanfy.helium.internal.MethodsExecutor
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.tests.BehaviourSuite

final class CheckableService extends Service implements BehaviorDescriptionContainer {

  private final List<BehaviourDescription> behaviourDescriptions = new ArrayList<>()

  @Override
  BehaviourSuite check(final MethodsExecutor executor) {
    return new CheckGroup(behaviourDescriptions, executor).run("Checks of ${this.name}")
  }

  @Override
  void addBehaviourDescription(final BehaviourDescription d) {
    d.service = this
    behaviourDescriptions.add(d)
  }

  int checksCount() {
    return behaviourDescriptions.size()
  }

}
