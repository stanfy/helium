package com.stanfy.helium.handler

import com.stanfy.helium.internal.dsl.HeliumScript
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Project
import org.codehaus.groovy.control.CompilerConfiguration

import java.nio.charset.Charset

/**
 * Extends project with script.
 */
class ScriptExtender implements Handler {

  /** Default user script path. */
  private static final String DEFAULT_USER_PATH = "/user/default";
  /** Default file name. */
  private static final String DEFAULT_FILE_NAME = "Project.spec";

  /** Script. */
  private final HeliumScript initScript
  /** Source. */
  private final GroovyCodeSource initSource
  /** Base directory for the script. */
  private final File baseDir

  /** Defined variables. */
  private Binding vars

  public ScriptExtender(final HeliumScript script) {
    this.initScript = script
    this.initSource = null
    this.baseDir = null;
  }

  public ScriptExtender(final Reader scriptReader) {
    this(scriptReader, DEFAULT_FILE_NAME, DEFAULT_USER_PATH)
  }

  public ScriptExtender(final Reader scriptReader, final String name, final String path) {
    this.initSource = new GroovyCodeSource(scriptReader, name, path)
    this.initScript = null
    this.baseDir = new File(path)
  }

  public static ScriptExtender fromFile(final File scriptFile, final Charset encoding) throws IOException {
    return new ScriptExtender(
        new InputStreamReader(new FileInputStream(scriptFile), encoding),
        scriptFile.getName().replaceAll(/\W+/, "_"),
        scriptFile.parentFile.absolutePath
    )
  }

  ScriptExtender withVars(final Binding vars) {
    this.vars = new Binding(new HashMap(vars.getVariables()))
    if (baseDir && !this.vars.hasVariable("baseDir")) {
      this.vars.setVariable("baseDir", baseDir)
    }
    return this
  }

  @Override
  public void handle(final Project project) {
    final HeliumScript script
    if (initScript) {
      script = initScript
    } else {
      def classLoader = Thread.currentThread().contextClassLoader
      CompilerConfiguration config = new CompilerConfiguration()
      config.scriptBaseClass = HeliumScript.canonicalName
      if (!vars) {
        vars = new Binding()
      }
      script = new GroovyShell(classLoader, vars, config).parse(initSource) as HeliumScript
    }

    script.setProject((ProjectDsl)project)
    script.run()
  }

}
