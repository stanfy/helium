package com.stanfy.helium

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.ClosureExtender
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.handler.ScriptExtender
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

  /** DSL instance to build. */
  private final ProjectDsl project = new ProjectDsl()

  /** Default types flag. */
  private boolean defaultTypes

  /** Encoding used to read files. */
  private String encoding = "UTF-8"

  Helium defaultTypes() {
    if (!this.defaultTypes) {
      this.defaultTypes = true
      DefaultTypesLoader.loadFor project
    }
    return this
  }

  Helium encoding(String encoding) {
    if (!encoding) { throw new IllegalArgumentException("Encoding is not specified") }
    this.encoding = encoding
    return this
  }

  Helium from(final Closure<?> spec) {
    new ClosureExtender(spec).handle(project)
    return this
  }

  Helium from(final Reader scriptReader) {
    new ScriptExtender(scriptReader, DEFAULT_FILE_NAME, DEFAULT_USER_PATH).handle(project)
    return this
  }

  Helium from(final File scriptFile) {
    new ScriptExtender(new InputStreamReader(new FileInputStream(scriptFile), encoding),
        scriptFile.name.replaceAll(/\W+/, "_"), DEFAULT_USER_PATH).handle(project)
    return this
  }

  Helium from(final String scriptText) {
    new ScriptExtender(new StringReader(scriptText), DEFAULT_FILE_NAME, DEFAULT_USER_PATH).handle(project)
    return this
  }

  Helium processBy(final Handler handler) {
    if (!handler) { throw new IllegalArgumentException("Handler is not specified") }
    handler.handle(project)
    return this
  }

}
