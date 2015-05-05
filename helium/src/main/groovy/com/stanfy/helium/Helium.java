package com.stanfy.helium;

import com.stanfy.helium.internal.dsl.ProjectDsl;
import com.stanfy.helium.handler.ClosureExtender;
import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.ScriptExtender;
import com.stanfy.helium.model.Project;
import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Main entry point for Java API.
 */
public final class Helium {

  /** DSL instance to build. */
  private final ProjectDsl project = new ProjectDsl();

  /** Default types flag. */
  private boolean defaultTypes;

  /** Encoding used to read files. */
  private String encoding = "UTF-8";

  public Project getProject() {
    return project;
  }

  public Helium defaultTypes() {
    if (!this.defaultTypes) {
      this.defaultTypes = true;
      DefaultTypesLoader.loadFor(project);
    }

    return this;
  }

  public Helium encoding(final String encoding) {
    if (encoding == null || encoding.length() == 0) {
      throw new IllegalArgumentException("Encoding is not specified");
    }

    this.encoding = encoding;
    return this;
  }

  public Helium from(final Closure<?> spec) {
    return processBy(new ClosureExtender(spec));
  }

  public Helium from(final Reader scriptReader) {
    return processBy(new ScriptExtender(scriptReader).withVars(project.getVariablesBinding()));
  }

  public Helium from(final File scriptFile) throws IOException {
    return processBy(ScriptExtender.fromFile(scriptFile, Charset.forName(encoding))
        .withVars(project.getVariablesBinding()));
  }

  public Helium from(final String scriptText) {
    return processBy(new ScriptExtender(new StringReader(scriptText)).withVars(project.getVariablesBinding()));
  }

  public Helium processBy(final Handler handler) {
    if (handler == null) {
      throw new IllegalArgumentException("Handler is not specified");
    }

    handler.handle(project);
    return this;
  }

  public Helium set(final String name, final Object value) {
    project.getVariablesBinding().setVariable(name, value);
    return this;
  }

}
