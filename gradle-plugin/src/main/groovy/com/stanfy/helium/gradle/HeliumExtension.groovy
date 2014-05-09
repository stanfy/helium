package com.stanfy.helium.gradle

import com.stanfy.helium.gradle.tasks.BaseHeliumTask
import com.stanfy.helium.utils.DslUtils
import groovy.transform.PackageScope
import org.apache.commons.io.FilenameUtils
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection

import static com.stanfy.helium.gradle.UserConfig.specName

/**
 * Helium extension.
 */
class HeliumExtension {

  public static final String VERSION = "0.3.7"

  /** Tasks group. */
  public static final String GROUP = "Helium"

  private static final String HELIUM_ARTIFACT_NAME = "helium"
  public static final String HELIUM_DEP = "com.stanfy.helium:$HELIUM_ARTIFACT_NAME"

  /** Specifications collection. */
  private final ArrayList<File> specifications = new ArrayList<>()

  /** Ignore test failures. */
  boolean ignoreFailures

  /** Classpath extension for Helium specs. */
  FileCollection classpath

  /** Source generation tasks for each specification. */
  private final Map<String, SourceGenerationTasks> sourceGenTasks = new HashMap<>()

  private UserConfig config

  @PackageScope
  void attach(final UserConfig config) {
    this.config = config
  }

  Collection<File> getSpecifications() {
    return specifications
  }

  final void specification(def spec) {
    specification(spec, null)
  }

  void specification(def spec, Closure<Void> config) {
    File specFile = this.config.project.file(spec)
    String name = specName(specFile)
    if (sourceGenTasks[name]) {
      throw new GradleException("Helium specification with name $name is already defined")
    }
    sourceGenTasks[name] = new SourceGenerationTasks()
    specifications.add specFile

    if (config) {
      SpecificationDslDelegate delegate = new SpecificationDslDelegate(specFile, this.config)
      DslUtils.runWithProxy(delegate, config)
    }
  }

  void sourceGen(Closure<?> config) {
    this.config.defaultSourceGeneration = new SourceGenDslDelegate(config.owner)
    DslUtils.runWithProxy(this.config.defaultSourceGeneration, config)
  }

  SourceGenerationTasks getSourceGen() {
    if (sourceGenTasks.empty) {
      return null
    }
    if (sourceGenTasks.size() == 1) {
      return sourceGenTasks.values().iterator().next()
    } else {
      throw new GradleException("Default sourceGen property cannot be accessed when "
          + "multiple specifications are declared. Use sourceGen('<specName>') instead.")
    }
  }

  SourceGenerationTasks sourceGen(final String name) {
    if (!name) {
      throw new IllegalArgumentException("Specification name is not provided")
    }
    return sourceGenTasks[name]
  }

  SourceGenerationTasks sourceGen(final File spec) {
    if (!spec) {
      throw new IllegalArgumentException("Specification is not provided")
    }
    return sourceGenTasks[specName(spec)]
  }

  public static class SourceGenerationTasks {
    Map<String, BaseHeliumTask> entities = [:]
    Map<String, BaseHeliumTask> constants = [:]
  }

}
