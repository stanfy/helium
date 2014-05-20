package com.stanfy.helium.handler.codegen.tests

import com.squareup.javawriter.JavaWriter
import com.stanfy.helium.HeliumWriter
import com.stanfy.helium.model.HttpHeader
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.tests.MethodTestInfo
import groovy.transform.CompileStatic

import javax.lang.model.element.Modifier

/**
 * REST API tests generator.
 * Generates JUnit4 tests that invoke REST API methods.
 */
@CompileStatic
class RestApiPokeTestsGenerator extends BaseUnitTestsGenerator {

  public RestApiPokeTestsGenerator(final File srcOutput) {
    this(srcOutput, null);
  }
  public RestApiPokeTestsGenerator(final File srcOutput, final File resourcesOutput) {
    this(srcOutput, resourcesOutput, null);
  }
  public RestApiPokeTestsGenerator(final File srcOutput, final File resourcesOutput, final String packageName) {
    super(srcOutput, resourcesOutput, packageName);
  }

  @Override
  protected String getClassName(final Service service) {
    return "${service.canonicalName}PokeTest"
  }

  @Override
  void handle(final Project project) {
    HeliumWriter specWriter = new HeliumWriter(new OutputStreamWriter(new FileOutputStream(specFile), UTF_8))
    try {
      project.types.all().each { Type type ->
        specWriter.writeType(type)
      }
    } finally {
      specWriter.close()
    }

    JsonEntityExampleGenerator entitiesGenerator = new JsonEntityExampleGenerator(project.getTypes())

    eachService(project, { final Service service, final JavaWriter writer ->
      service.methods.each { ServiceMethod method ->
        addTestMethods writer, service, method, entitiesGenerator
      }
    } as BaseUnitTestsGenerator.ServiceHandler)
  }

  private static void addTestMethods(final JavaWriter out, final Service service, ServiceMethod method,
                                     final JsonEntityExampleGenerator entitiesGenerator) {
    MethodTestInfo testInfo = prepareTestInfo(method, service)
    if (!allHeadersResolved(method, testInfo)) {
      return
    }

    String encoding = HttpExecutor.resolveEncoding(service, method)

    MethodGenerator gen = new MethodGenerator(out: out, service: service, method: method, testInfo: testInfo)

    boolean pathExamplesPresent = testInfo.pathExample && !testInfo.pathExample.empty

    if (testInfo.generateBadInputTests && method.hasRequiredParameters()) {

      if (!method.hasRequiredParametersInPath() || pathExamplesPresent) {
        // make test without required parameters - method should fail
        gen.method("_shouldFailWithOutParameters", service.getMethodUri(testInfo, method)) {
          gen.expectClientError()
        }
      }

    }

    boolean requestUriReady = !method.hasRequiredParameters()
    String parametrizedUri = service.getMethodUri(testInfo, method)

    if (testInfo.useExamples) {

      final String uriQueryExample = method.getUriQueryWithExamples(encoding)
      if (method.hasRequiredParameterFields()) {
        requestUriReady |= !uriQueryExample.empty
      }
      if (method.hasRequiredParametersInPath()) {
        requestUriReady |= pathExamplesPresent
      }

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
      if (testInfo.generateBadInputTests) {
        gen.method("_shouldFailWithOutBody", parametrizedUri) {
          gen.expectClientError()
        }
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

  private static MethodTestInfo prepareTestInfo(ServiceMethod method, Service service) {
    MethodTestInfo testInfo = method.testInfo.resolve(service.testInfo)
    method.httpHeaders.each { HttpHeader h ->
      if (h.constant) {
        testInfo.httpHeaders[h.name] = h.value
      } else if (h.examples?.size() > 0) {
        testInfo.httpHeaders[h.name] = h.examples[0]
      }
    }
    return testInfo
  }

  private static boolean allHeadersResolved(ServiceMethod method, MethodTestInfo testInfo) {
    return method.httpHeaders.findAll() { HttpHeader h -> !h.constant }.every { HttpHeader h ->
      testInfo.httpHeaders.containsKey(h.name)
    }
  }

  private static class MethodGenerator {

    JavaWriter out
    ServiceMethod method
    Service service
    MethodTestInfo testInfo

    private void emitHeaders() {
      testInfo.httpHeaders.each { String key, String value ->
        out.emitStatement('request.addHeader(%s, %s)', JavaWriter.stringLiteral(key), JavaWriter.stringLiteral(value))
      }
    }

    private void sendRequestBody(final String uri, final String body) {
      out.emitStatement("HttpRequestBase request = createHttpRequest(MethodType.%s)", method.type.toString())
      out.emitStatement('request.setURI(new URI(%s))', JavaWriter.stringLiteral(uri))
      if (body) {
        out.emitStatement('HttpEntity requestEntity = new StringEntity(%s)', JavaWriter.stringLiteral(body))
        out.emitStatement('((HttpEntityEnclosingRequestBase)request).setEntity(requestEntity)')
      }
      emitHeaders()
      out.emitStatement('HttpResponse response = send(request)')
    }

    private void startTestMethod(String nameSuffix) {
      String name = method.canonicalName
      out.emitAnnotation('Test')
      out.beginMethod('void', name + nameSuffix, Collections.<Modifier>singleton(Modifier.PUBLIC), null,
          [Exception.simpleName])
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
      out.emitStatement('validateStatus(request, response, %s)', success ? "true" : "false")
    }

    private void validateBody(final String encoding) {
      if (method.response) {
        out.emitStatement('validate(request, response, "%s", %s)', encoding,
            JavaWriter.stringLiteral(method.response.name))
      }
    }

  }

}

