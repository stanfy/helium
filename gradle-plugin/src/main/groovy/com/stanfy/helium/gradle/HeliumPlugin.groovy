package com.stanfy.helium.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin for Helium.
 */
class HeliumPlugin implements Plugin<Project> {

  @Override
  void apply(final Project project) {
    project.extensions.add("helium", HeliumExtension)
    HeliumExtension hel = project.helium
    hel.attach project
  }

}
