package com.stanfy.helium.handler.codegen.tests

import com.squareup.javawriter.JavaWriter
import com.stanfy.helium.HeliumWriter
import com.stanfy.helium.handler.tests.JsonEntityExampleGenerator
import com.stanfy.helium.handler.tests.NoExamplesProvidedException
import com.stanfy.helium.internal.dsl.FieldsBuilder
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Service
import com.stanfy.helium.model.ServiceMethod
import com.stanfy.helium.model.Type
import com.stanfy.helium.model.tests.MethodTestInfo
import groovy.transform.CompileStatic

import javax.lang.model.element.Modifier

import static com.stanfy.helium.handler.codegen.tests.Utils.preparePokeTestInfo
import static com.stanfy.helium.handler.tests.Utils.findUnresolvedHeaders
import static com.stanfy.helium.handler.tests.Utils.resolveEncoding

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
    super(srcOutput, resourcesOutput, packageName, "poke");
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
      int count = 0;
      service.methods.each { ServiceMethod method ->
        count += addTestMethods writer, service, method, entitiesGenerator
      }
      return count > 0
    } as BaseUnitTestsGenerator.ServiceHandler)
  }

  private static int addTestMethods(final JavaWriter out, final Service service, ServiceMethod method,
                                     final JsonEntityExampleGenerator entitiesGenerator) {
    MethodTestInfo testInfo = preparePokeTestInfo(method, service)
    if (!findUnresolvedHeaders(method, testInfo.httpHeaders).empty) {
      return 0
    }

    String encoding = resolveEncoding(service, method)

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
          gen.expectSuccess()
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
            gen.expectSuccess()
          }
        } catch (NoExamplesProvidedException ignored) {
          // ignore
        }
      }

    }

    return gen.count
  }

  private static class MethodGenerator {

    JavaWriter out
    ServiceMethod method
    Service service
    MethodTestInfo testInfo

    int count

    private void createRequest(final String uri, final String body) {
      // TODO: Support more content types.
      out.emitStatement("MediaType requestContentType = MediaType.parse(%s)",
          JavaWriter.stringLiteral("application/json"))
      out.emitStatement("""
        Request.Builder rb = new Request.Builder()
            .method(%s, ${body != null ? 'RequestBody.create(requestContentType, %s)' : '%s'})
            .url(%s)
      """.trim(),
          JavaWriter.stringLiteral(method.type.toString()),
          body != null ? JavaWriter.stringLiteral(body) : null,
          JavaWriter.stringLiteral(uri))
      testInfo.httpHeaders.each { String key, String value ->
        out.emitStatement('rb.header(%s, %s)', JavaWriter.stringLiteral(key), JavaWriter.stringLiteral(value))
      }
      out.emitStatement("Request request = rb.build()")
    }

    private void invokeRequest() {
      out.emitStatement("Response response = getClient().newCall(request).execute()")
    }

    private void startTestMethod(String nameSuffix) {
      count++
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
      createRequest(url, requestBody)
      invokeRequest()
      methodBody.call()
      out.endMethod()
    }

    void expectSuccess() {
      validateStatusCode(true)
      validateBody()
    }

    void expectClientError() {
      validateStatusCode(false)
    }

    private void validateStatusCode(final boolean success) {
      out.emitStatement('validateStatus(response, %s)', success ? "true" : "false")
    }

    private void validateBody() {
      if (method.response) {
        out.emitStatement('validate(response, %s)', JavaWriter.stringLiteral(method.response.name))
      }
    }

  }

}

