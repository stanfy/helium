package com.stanfy.helium.handler.codegen.tests

import com.squareup.javawriter.JavaWriter
import com.stanfy.helium.HeliumWriter
import com.stanfy.helium.handler.Handler
import com.stanfy.helium.model.MethodType
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.tests.MethodTestInfo
import groovy.transform.CompileStatic
import org.apache.http.HttpResponse
import org.apache.http.client.methods.*
import org.apache.http.impl.client.HttpClientBuilder

import javax.lang.model.element.Modifier

/**
 * REST API tests generator.
 * Generates JUnit4 tests that invoke REST API methods.
 */
@CompileStatic
class RestApiTestsGenerator implements Handler {

  // TODO: use examples

  /** Output directory. */
  File srcOutput

  /** Resources output. */
  File resourcesOutput

  /** Package name for tests. */
  String packageName = "spec.tests.rest"

  /** User agent string. */
  String userAgent

  @Override
  void handle(final Project project) {
    if (!srcOutput) { throw new IllegalStateException("Output is note defined") }
    if (!srcOutput.directory) { throw new IllegalStateException("Output is not a directory") }

    File sourcesPackageDir = new File(srcOutput, packageName.replaceAll(/\./, '/'))
    if (!resourcesOutput) {
      resourcesOutput = srcOutput
    }
    File resourcesPackageDir = new File(resourcesOutput, packageName.replaceAll(/\./, '/'))
    sourcesPackageDir.mkdirs()
    resourcesPackageDir.mkdirs()

    File specFile = new File(resourcesPackageDir, RestApiMethods.TEST_SPEC_NAME)
    HeliumWriter specWriter = new HeliumWriter(new OutputStreamWriter(new FileOutputStream(specFile), "UTF-8"))
    try {
      specWriter.writeProject(project)
    } finally {
      specWriter.close()
    }

    project.services.each { Service service ->
      String className = "${service.canonicalName}Test"
      File destination = new File(sourcesPackageDir, "${className}.java")
      def out = new OutputStreamWriter(new FileOutputStream(destination), "UTF-8")

      JavaWriter writer = new JavaWriter(out).emitPackage(packageName)

      try {
        writer
            .emitImports("org.junit.Test")
            .emitImports("org.apache.http.client.methods.*")
            .emitImports(RestApiMethods.name, URI.name, HttpResponse.name)
            .emitStaticImports("org.fest.assertions.api.Assertions.assertThat")

        writer.beginType(className, 'class', Collections.<Modifier>singleton(Modifier.PUBLIC), RestApiMethods.simpleName)

        if (userAgent) {
          writer.beginMethod(HttpClientBuilder.name, "httpClientBuilder", Collections.singleton(Modifier.PROTECTED))
          writer.emitStatement('return super.httpClientBuilder().setUserAgent("%s")', userAgent)
          writer.endMethod()
        }

        service.methods.each { ServiceMethod method -> addTestMethods writer, service, method }

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

    MethodTestInfo testInfo = method.testInfo.resolve(service.testInfo)

    if (method.parameters?.hasRequiredFields()) {

      // make test without required parameters - should fail
      startTestMethod(out, method, "_shouldFailWithOutParameters")
      sendRequestBody(out, method.type, service.getMethodUri(testInfo, method), testInfo.httpHeaders)
      validateStatusCode(out, false)
      out.endMethod()

    }

    if (testInfo.useExamples) {

      String uriQuery = method.getUriQueryWithExamples(encoding)
      if (uriQuery || !method.parameters?.hasRequiredFields()) {
        // can make an example
        startTestMethod(out, method, "_example")
        sendRequestBody(out, method.type, "${service.getMethodUri(testInfo, method)}$uriQuery", testInfo.httpHeaders)
        validateStatusCode(out, true)
        validateBody(out, encoding, method)
        out.endMethod()
      }

    }

  }

  private static void validateStatusCode(final JavaWriter out, final boolean success) {
    out.emitStatement('validateStatus(response, %s)', success ? "true" : "false")
  }

  private static void validateBody(final JavaWriter out, final String encoding, final ServiceMethod method) {
    out.emitStatement('validate(response, "%s", "%s")', encoding, method.response.name)
  }

  private static void sendRequestBody(final JavaWriter out, final MethodType type, final String uri, final Map<String, String> headers) {
    String requestClass = getRequestClass(type)
    out.emitStatement("$requestClass request = new ${requestClass}()")
    out.emitStatement('request.setURI(new URI("%s"))', uri)
    headers.each { String key, String value ->
      out.emitStatement('request.addHeader("%s", "%s")', key, value)
    }
    out.emitStatement('HttpResponse response = send(request)')
  }
  private static void startTestMethod(final JavaWriter out, ServiceMethod method, String nameSuffix) {
    String name = method.canonicalName
    out.emitAnnotation('Test')
    out.beginMethod('void', name + nameSuffix, Collections.<Modifier>singleton(Modifier.PUBLIC), null, [Exception.simpleName])
  }
}
