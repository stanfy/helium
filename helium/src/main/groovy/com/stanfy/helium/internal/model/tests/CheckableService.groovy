package com.stanfy.helium.internal.model.tests

import com.stanfy.helium.model.Service
import com.stanfy.helium.model.tests.BehaviourSuite

final class CheckableService extends Service implements BehaviorDescriptionContainer {

  private final List<BehaviourDescription> behaviourDescriptions = new ArrayList<>()

  @Override
  BehaviourSuite check() {
    return new CheckGroup(behaviourDescriptions).run("Checks of ${this.name}")
  }

  @Override
  void addBehaviourDescription(final BehaviourDescription d) {
    behaviourDescriptions.add(d)
  }

  int checksCount() {
    return behaviourDescriptions.size()
  }

}
