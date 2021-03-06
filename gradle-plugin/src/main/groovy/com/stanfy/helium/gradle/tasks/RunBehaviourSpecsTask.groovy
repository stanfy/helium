package com.stanfy.helium.gradle.tasks

import com.stanfy.helium.handler.tests.BehaviorSpecRunner
import com.stanfy.helium.handler.tests.BehaviorSpecRunnerOptions
import org.gradle.api.GradleException

/**
 * Task for running behaviour specs.
 */
class RunBehaviourSpecsTask extends BaseHeliumTask<BehaviorSpecRunnerOptions> {

  boolean ignoreFailures

  @Override
  protected void doIt() {
    if (options == null) {
      options = new BehaviorSpecRunnerOptions()
    }

    def runner = new BehaviorSpecRunner(options, output)
    helium.processBy(runner)
    if (!runner.passed()) {
      def message = "Behaviour checks failed! See report at $output/html/index.html"
      if (!ignoreFailures) {
        throw new GradleException(message)
      }
      project.logger.warn(message)
    }
  }

}
