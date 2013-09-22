package com.stanfy.helium

import com.stanfy.helium.dsl.Project
import com.stanfy.helium.handler.ClosureExtender
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.ScriptExtender
import com.stanfy.helium.utils.DefaultTypesLoader
import groovy.transform.CompileStatic

/**
 * Main access point.
 */
@CompileStatic
class Helium {

  /** Default user script path. */
  private static final String DEFAULT_USER_PATH = "/user/default"
  /** Default file name. */
  private static final String DEFAULT_FILE_NAME = "Project.spec"

  /** Initial project builder. */
  private Handler builder

  /** DSL instance to build. */
  private Project project

  /** Default types flag. */
  private boolean defaultTypes

  /** Encoding used to read files. */
  private String encoding = "UTF-8"

  Helium defaultTypes(boolean value) {
    this.defaultTypes = value
    return this
  }

  Helium encoding(String encoding) {
    if (!encoding) { throw new IllegalArgumentException("Encoding is not specified") }
    this.encoding = encoding
    return this
  }

  Helium from(final Closure<?> spec) {
    builder = new ClosureExtender(spec)
    return this
  }

  Helium from(final Reader scriptReader) {
    builder = new ScriptExtender(scriptReader, DEFAULT_FILE_NAME, DEFAULT_USER_PATH)
    return this
  }

  Helium from(final File scriptFile) {
    builder = new ScriptExtender(new InputStreamReader(new FileInputStream(scriptFile), encoding),
        scriptFile.name.replaceAll(/\W+/, "_"), DEFAULT_USER_PATH)
    return this
  }

  Helium processBy(final Handler handler) {
    if (!handler) { throw new IllegalArgumentException("Handler is not specified") }

    // 1. build
    if (!project) {
      project = new Project()
      if (defaultTypes) { DefaultTypesLoader.loadFor project }
      builder.handle(project)
    }

    // 2. process
    handler.handle(project)

    return this
  }

}
