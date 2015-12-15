package com.stanfy.helium.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import com.stanfy.helium.gradle.internal.SourceCodeGenerators
import com.stanfy.helium.gradle.tasks.BaseHeliumTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Integrates Helium with Android plugin.
 */
class AndroidPlugin implements Plugin<Project> {

  @Override
  void apply(final Project project) {
    project.apply plugin: HeliumPlugin

    if (!project.plugins.hasPlugin("com.android.application")
        && !project.plugins.hasPlugin("com.android.library")) {
      throw new GradleException("Android plugin is not applied");
    }

    project.afterEvaluate {
      addGeneratedSources(project)
    }
  }

  @groovy.transform.PackageScope
  static void addGeneratedSources(final Project project) {
    HeliumExtension hel = project.helium
    def generators = SourceCodeGenerators.java()
    def allTasks = hel.specifications.collect { File spec ->
      generators.collect { hel.sourceGen(spec)[it] }.findAll { it != null }
    }.flatten()

    def variants = (project.plugins.findPlugin(AppPlugin.class)
        ? project.android.applicationVariants
        : project.android.libraryVariants)
    variants.all { BaseVariant variant ->
      allTasks.each { BaseHeliumTask task ->
        variant.registerJavaGeneratingTask(task, task.output)
      }
    }
  }


}
