package com.stanfy.helium.utils

import com.stanfy.helium.dsl.Dsl
import com.stanfy.helium.dsl.HeliumScript
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Default types loader.
 */
@CompileStatic
class DefaultTypesLoader {

  static Reader openScript() {
    return new InputStreamReader(DefaultTypesLoader.classLoader.getResourceAsStream("com/stanfy/helium/dsl/def-types.spec"), "UTF-8")
  }

  static void loadFor(Dsl dsl) {
    CompilerConfiguration config = new CompilerConfiguration()
    config.scriptBaseClass = HeliumScript.canonicalName
    GroovyCodeSource source = new GroovyCodeSource(openScript(), "DefTypes.spec", "/helium/default")
    HeliumScript script = new GroovyShell(new Binding(), config).parse(source) as HeliumScript
    script.setDsl dsl
    script.run()
  }

}
