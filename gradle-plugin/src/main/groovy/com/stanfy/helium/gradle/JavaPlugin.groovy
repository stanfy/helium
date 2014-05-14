package com.stanfy.helium.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

/**
 * Integrates Helium with standard Java project.
 */
class JavaPlugin implements Plugin<Project> {

  @Override
  void apply(final Project project) {
    project.apply plugin: 'helium'

    if (!project.plugins.findPlugin(org.gradle.api.plugins.JavaPlugin.class)) {
      throw new GradleException("Java plugin is not applied");
    }

    project.afterEvaluate {
      addGeneratedSources(project)
    }
  }

  @groovy.transform.PackageScope
  static void addGeneratedSources(final Project project) {
    HeliumExtension hel = project.helium
    def generators = ['entities', 'constants', 'retrofit'] // TODO stop hardcoding generators
    def allTasks = hel.specifications.collect { File spec ->
      generators.collect { hel.sourceGen(spec)[it].values() }
    }.flatten()

    SourceSet main = project.sourceSets.main
    main.java.srcDirs += allTasks.collect() { it.output }

    project.tasks['compileJava'].dependsOn allTasks
  }

}
