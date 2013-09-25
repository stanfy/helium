package com.stanfy.helium.handler.codegen.tests

import com.squareup.javawriter.JavaWriter
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod

import javax.lang.model.element.Modifier

/**
 * REST API tests generator.
 * Generates JUnit4 tests that invoke REST API methods.
 */
class RestApiTestsGenerator implements Handler {

  /** Output directory. */
  File output

  /** Package name for tests. */
  String packageName = "spec.tests.rest"

  @Override
  void handle(final Project project) {
    if (!output) { throw new IllegalStateException("Output is note defined") }
    if (!output.directory) { throw new IllegalStateException("Output is not a directory") }

    project.services.each { Service service ->
      String className = "${service.canonicalName}Test"
      File destination = new File(output, "${packageName.replaceAll(/\./, '/')}/${className}.java")
      destination.parentFile.mkdirs()
      def out = new OutputStreamWriter(new FileOutputStream(destination), "UTF-8")

      JavaWriter writer = new JavaWriter(out).emitPackage(packageName)
      writer.emitImports("org.junit.Test").emitStaticImports("org.fest.assertions.api.Assertions.assertThat")
      writer.beginType(className, 'class', Collections.<Modifier>singleton(Modifier.PUBLIC))
      service.methods.each { addTestMethod writer, it }
      writer.endType()
      writer.close()
    }
  }

  private void addTestMethod(final JavaWriter out, ServiceMethod method) {

  }

}
