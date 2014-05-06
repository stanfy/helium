package com.stanfy.helium.gradle

import com.stanfy.helium.gradle.tasks.BaseHeliumTask
import com.stanfy.helium.utils.DslUtils
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

/**
 * Helium extension.
 */
class HeliumExtension {

  public static final String VERSION = "0.3.7-SNAPSHOT"

  /** Tasks group. */
  public static final String GROUP = "Helium"

  private static final String HELIUM_ARTIFACT_NAME = "helium"
  public static final String HELIUM_DEP = "com.stanfy.helium:$HELIUM_ARTIFACT_NAME"

  /** Specification location. */
  File specification

  /** Ignore test failures. */
  boolean ignoreFailures

  /** Classpath extension for Helium specs. */
  FileCollection classpath

  private final SourceGenerationTasks sourceGenTasks = new SourceGenerationTasks()

  private Config config

  @PackageScope
  void attach(final Config config) {
    this.config = config
  }

  void sourceGen(Closure<?> config) {
    this.config.sourceGeneration = new SourceGenDslDelegate(config.owner)
    DslUtils.runWithProxy(this.config.sourceGeneration, config)
  }

  SourceGenerationTasks getSourceGen() {
    return sourceGenTasks
  }

  public static class SourceGenerationTasks {
    Map<String, BaseHeliumTask> entities = [:]
    Map<String, BaseHeliumTask> constants = [:]
  }

}
