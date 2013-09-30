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
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity

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

    JsonEntityExampleGenerator entitiesGenerator = new JsonEntityExampleGenerator(project.getTypes())

    project.services.each { Service service ->
      String className = "${service.canonicalName}Test"
      File destination = new File(sourcesPackageDir, "${className}.java")
      def out = new OutputStreamWriter(new FileOutputStream(destination), "UTF-8")

      JavaWriter writer = new JavaWriter(out).emitPackage(packageName)

      try {
        writer
            .emitImports("org.junit.Test")
            .emitImports("org.apache.http.client.methods.*")
            .emitImports(RestApiMethods.name, URI.name, HttpResponse.name, HttpEntity.name, StringEntity.name)
            .emitStaticImports("org.fest.assertions.api.Assertions.assertThat")

        writer.beginType(className, 'class', Collections.<Modifier>singleton(Modifier.PUBLIC), RestApiMethods.simpleName)

//        if (userAgent) {
//          writer.beginMethod(HttpClientBuilder.name, "httpClientBuilder", Collections.singleton(Modifier.PROTECTED))
//          writer.emitStatement('return super.httpClientBuilder().setUserAgent("%s")', userAgent)
//          writer.endMethod()
//        }

        service.methods.each { ServiceMethod method -> addTestMethods writer, service, method, entitiesGenerator }

        writer.endType()
      } finally {
        writer.close()
      }

    }
  }

  private static void addTestMethods(final JavaWriter out, final Service service, ServiceMethod method, final JsonEntityExampleGenerator entitiesGenerator) {
    String encoding = method.encoding
    if (!encoding) { encoding = service.encoding }
    if (!encoding) { encoding = 'UTF-8' }

    MethodTestInfo testInfo = method.testInfo.resolve(service.testInfo)

    MethodGenerator gen = new MethodGenerator(out: out, service: service, method: method, testInfo: testInfo)

    if (method.parameters?.hasRequiredFields()) {

      // make test without required parameters - should fail
      gen.method("_shouldFailWithOutParameters", service.getMethodUri(testInfo, method)) {
        gen.expectClientError()
      }

    }

    boolean requestUriReady = !method.parameters?.hasRequiredFields()
    String parametrizedUri = service.getMethodUri(testInfo, method)

    if (testInfo.useExamples) {

      final String uriQueryExample = method.getUriQueryWithExamples(encoding)
      requestUriReady = requestUriReady | !uriQueryExample.empty
      parametrizedUri = "${parametrizedUri}$uriQueryExample"

      if (requestUriReady && !method.hasBody()) {
        // can make an example
        gen.method("_example", parametrizedUri) {
          gen.expectSuccess(encoding)
        }
      }

    }

    if (method.hasBody() && requestUriReady) {

      // make test without body - should fail
      gen.method("_shouldFailWithOutBody", parametrizedUri) {
        gen.expectClientError()
      }

      if (testInfo.useExamples) {
        try {
          gen.method("_example", parametrizedUri, entitiesGenerator.generate(method.body).toString()) {
            gen.expectSuccess(encoding)
          }
        } catch (NoExamplesProvidedException ignored) {
          // ignore
        }
      }

    }


  }

  private static class MethodGenerator {

    JavaWriter out
    ServiceMethod method
    Service service
    MethodTestInfo testInfo

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

    private void emitHeaders() {
      testInfo.httpHeaders.each { String key, String value ->
        out.emitStatement('request.addHeader(%s, %s)', JavaWriter.stringLiteral(key), JavaWriter.stringLiteral(value))
      }
    }

    private void sendRequestBody(final String uri, final String body) {
      String requestClass = getRequestClass(method.type)
      out.emitStatement("$requestClass request = new ${requestClass}()")
      out.emitStatement('request.setURI(new URI(%s))', JavaWriter.stringLiteral(uri))
      if (body) {
        out.emitStatement('HttpEntity requestEntity = new StringEntity(%s)', JavaWriter.stringLiteral(body))
        out.emitStatement('request.setEntity(requestEntity)')
      }
      emitHeaders()
      out.emitStatement('HttpResponse response = send(request)')
    }

    private void startTestMethod(String nameSuffix) {
      String name = method.canonicalName
      out.emitAnnotation('Test')
      out.beginMethod('void', name + nameSuffix, Collections.<Modifier>singleton(Modifier.PUBLIC), null, [Exception.simpleName])
    }

    void method(final String suffix, final String url, final Closure<?> methodBody) {
      method(suffix, url, null, methodBody)
    }

    void method(final String suffix, final String url, final String requestBody, final Closure<?> methodBody) {
      startTestMethod(suffix)
      sendRequestBody(url, requestBody)
      methodBody.call()
      out.endMethod()
    }

    void expectSuccess(final String encoding) {
      validateStatusCode(true)
      validateBody(encoding)
    }

    void expectClientError() {
      validateStatusCode(false)
    }

    private void validateStatusCode(final boolean success) {
      out.emitStatement('validateStatus(response, %s)', success ? "true" : "false")
    }

    private void validateBody(final String encoding) {
      if (!method.response) { throw new IllegalStateException("Method response is not defined") }
      out.emitStatement('validate(response, "%s", %s)', encoding, JavaWriter.stringLiteral(method.response.name))
    }

  }

}

