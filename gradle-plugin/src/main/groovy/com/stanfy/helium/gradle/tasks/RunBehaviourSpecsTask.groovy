package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.tests.BehaviorSpecRunner
import com.stanfy.helium.handler.tests.BehaviorSpecRunnerOptions
import org.gradle.api.GradleException

/**
 * Task for running behaviour specs.
 */
class RunBehaviourSpecsTask extends BaseHeliumTask<BehaviorSpecRunnerOptions> {

  @Override
  protected void doIt() {
    if (options == null) {
      options = new BehaviorSpecRunnerOptions()
    }

    def runner = new BehaviorSpecRunner(options, output)
    helium.processBy(runner)
    if (!runner.passed()) {
      throw new GradleException("Behaviour checks failed! See report at $output/html/index.html")
    }
  }

}
