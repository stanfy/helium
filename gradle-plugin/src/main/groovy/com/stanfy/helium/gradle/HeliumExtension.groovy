package com.stanfy.helium.gradle

import com.stanfy.helium.Helium
import org.gradle.api.Project

/**
 * Helium extension.
 */
class HeliumExtension {

  /** Helium. */
  private final Helium heliumInstance = new Helium().defaultTypes()

  /** Specification location. */
  File specification

  private Project project

  void attach(final Project project) {
    this.project = project
  }

  void setSpecification(final File file) {
    this.@specification = file
    heliumInstance.from file

    GenerateApiTestsTask task = project.tasks.create("genApiTests", GenerateApiTestsTask)
    task.helium = heliumInstance
    task.output = new File(project.buildDir, "source/api-spec/tests")
    task.input = file
  }

}
