package com.stanfy.helium.handler

import com.stanfy.helium.dsl.HeliumScript
import com.stanfy.helium.dsl.Project
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Extends project with script.
 */
class ScriptExtender implements Handler {

  /** Closure. */
  private final HeliumScript script

  public ScriptExtender(final HeliumScript script) {
    this.script = script
  }

  public ScriptExtender(final Reader scriptReader, final String name, final String path) {
    CompilerConfiguration config = new CompilerConfiguration()
    config.scriptBaseClass = HeliumScript.canonicalName
    GroovyCodeSource source = new GroovyCodeSource(scriptReader, name, path)
    this.script = new GroovyShell(new Binding(), config).parse(source) as HeliumScript
  }

  public void handle(final Project project) {
    script.setProject project
    script.run()
  }

}
