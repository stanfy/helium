package com.stanfy.helium;

import com.stanfy.helium.dsl.ProjectDsl;
import com.stanfy.helium.handler.ClosureExtender;
import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.ScriptExtender;
import com.stanfy.helium.model.Project;
import groovy.lang.Binding;
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

  /** Script variables. */
  private final Binding variables = new Binding();

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

  public Helium encoding(String encoding) {
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
    return processBy(new ScriptExtender(scriptReader).withVars(variables));
  }

  public Helium from(final File scriptFile) throws IOException {
    return processBy(ScriptExtender.fromFile(scriptFile, Charset.forName(encoding)).withVars(variables));
  }

  public Helium from(final String scriptText) {
    return processBy(new ScriptExtender(new StringReader(scriptText)).withVars(variables));
  }

  public Helium processBy(final Handler handler) {
    if (handler == null) {
      throw new IllegalArgumentException("Handler is not specified");
    }

    handler.handle(project);
    return this;
  }

  public Helium set(final String name, final Object value) {
    variables.setVariable(name, value);
    return this;
  }

}
