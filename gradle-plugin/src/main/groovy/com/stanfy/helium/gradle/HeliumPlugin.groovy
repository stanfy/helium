package com.stanfy.helium.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/**
 * Gradle plugin for Helium.
 */
class HeliumPlugin implements Plugin<Project> {

  private UserConfig config

  @Override
  void apply(final Project project) {
    Configuration configuration = project.configurations.create('helium')
    configuration.description = "Helium specification dependencies configuration"
    configuration.visible = false

    project.extensions.add("helium", HeliumExtension)
    HeliumExtension hel = project.helium
    config = new UserConfig(project)
    hel.attach config

    project.afterEvaluate {
      createTasks(project)
    }
  }

  void createTasks(final Project project) {
    HeliumExtension extension = project.helium

    def classpath = project.configurations.helium
    URL[] urls = classpath.collect() { it.toURI().toURL() } as URL[]

    new HeliumInitializer(extension, config).createTasks(urls)
  }

}
