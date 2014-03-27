package com.stanfy.helium.gradle

import com.stanfy.helium.dsl.HeliumScript
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin for Helium.
 */
class HeliumPlugin implements Plugin<Project> {

  private Config config

  @Override
  void apply(final Project project) {
    project.extensions.add("helium", HeliumExtension)
    HeliumExtension hel = project.helium
    config = new Config(project)
    hel.attach config

    project.afterEvaluate {
      createTasks(project)
    }
  }

  void createTasks(final Project project) {
    HeliumExtension extension = project.helium

    def classpath = extension.classpath ? extension.classpath : project.files()
    URL[] urls = classpath.collect() { it.toURI().toURL() } as URL[]
    URLClassLoader classLoader = new URLClassLoader(urls, HeliumScript.classLoader)

    new HeliumInitializer(extension, config).createTasks(classLoader)
  }

}
