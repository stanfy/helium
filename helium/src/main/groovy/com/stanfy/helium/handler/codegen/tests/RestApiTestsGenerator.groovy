package com.stanfy.helium.handler.codegen.tests

import com.squareup.javawriter.JavaWriter
import com.stanfy.helium.HeliumWriter
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut

import javax.lang.model.element.Modifier

/**
 * REST API tests generator.
 * Generates JUnit4 tests that invoke REST API methods.
 */
class RestApiTestsGenerator implements Handler {

  // TODO: use examples

  /** Output directory. */
  File output

  /** Package name for tests. */
  String packageName = "spec.tests.rest"

  @Override
  void handle(final Project project) {
    if (!output) { throw new IllegalStateException("Output is note defined") }
    if (!output.directory) { throw new IllegalStateException("Output is not a directory") }

    File packageDir = new File(output, packageName.replaceAll(/\./, '/'))
    packageDir.mkdirs()

    File specFile = new File(packageDir, RestApiMethods.TEST_SPEC_NAME)
    HeliumWriter specWriter = new HeliumWriter(new OutputStreamWriter(new FileOutputStream(specFile), "UTF-8"))
    try {
      specWriter.writeProject(project)
    } finally {
      specWriter.close()
    }

    project.services.each { Service service ->
      String className = "${service.canonicalName}Test"
      File destination = new File(packageDir, "${className}.java")
      def out = new OutputStreamWriter(new FileOutputStream(destination), "UTF-8")

      JavaWriter writer = new JavaWriter(out).emitPackage(packageName)

      try {
        writer
            .emitImports("org.junit.Test")
            .emitImports(RestApiMethods.name)
            .emitImports("org.apache.http.client.methods.*")
            .emitImports("org.apache.http.HttpResponse")
            .emitStaticImports("org.fest.assertions.api.Assertions.assertThat")

        writer.beginType(className, 'class', Collections.<Modifier>singleton(Modifier.PUBLIC), RestApiMethods.simpleName)
        service.methods.each { addTestMethods writer, service, it }
        writer.endType()
      } finally {
        writer.close()
      }

    }
  }

  private static String getRequestClass(final MethodType type) {
    switch (type) {
      case MethodType.GET:
        return HttpGet.simpleName;
      case MethodType.POST:
        return HttpPost.simpleName;
      case MethodType.PUT:
        return HttpPut.simpleName;
      case MethodType.DELETE:
        return HttpDelete.simpleName;
      case MethodType.PATCH:
        return HttpPatch.simpleName;
      default:
        throw new UnsupportedOperationException("Unknown method type " + type)
    }
  }

  private static void addTestMethods(final JavaWriter out, final Service service, ServiceMethod method) {
    String encoding = method.encoding
    if (!encoding) { encoding = service.encoding }
    if (!encoding) { encoding = 'UTF-8' }

    if (method.parameters && method.parameters.hasRequiredFields()) {
      startTestMethod(out, method, "_shouldFailWithOutParameters")
      sendRequestBody(out, service, method)
      validateStatusCode(out, false)
      out.endMethod()
    }

    startTestMethod(out, method, "_example")
    sendRequestBody(out, service, method)
    validateStatusCode(out, true)
    validateBody(out, encoding, method)

    out.endMethod()
  }

  private static void validateStatusCode(final JavaWriter out, final boolean success) {
    out.emitStatement('validateStatus(response, %s)', success ? "true" : "false")
  }

  private static void validateBody(final JavaWriter out, final String encoding, final ServiceMethod method) {
    out.emitStatement('validate(response, "%s", "%s")', encoding, method.response.name)
  }

  private static void sendRequestBody(final JavaWriter out, final Service service, ServiceMethod method, String... configureStatements) {
    String requestClass = getRequestClass(method.type)
    out.emitStatement("$requestClass request = new ${requestClass}()")
    out.emitStatement('request.setURI("%s")', service.getMethodUri(method))
    configureStatements.each {
      out.emitStatement(it)
    }
    out.emitStatement('HttpResponse response = send(request)')
  }
  private static void startTestMethod(final JavaWriter out, ServiceMethod method, String nameSuffix) {
    String name = method.canonicalName
    out.emitAnnotation('Test')
    out.beginMethod('void', name + nameSuffix, Collections.<Modifier>singleton(Modifier.PUBLIC), null, [Exception.simpleName])
  }
}
