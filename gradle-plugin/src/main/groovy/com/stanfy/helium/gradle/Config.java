package com.stanfy.helium.gradle;

import org.gradle.api.Project;

class Config {

  /** Gradle project. */
  final Project project;

  /** Source generation data. */
  SourceGenDslDelegate sourceGeneration;

  Config(final Project project) {
    this.project = project;
  }

}
