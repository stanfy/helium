package com.stanfy.helium.handler.codegen.tests;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.stanfy.helium.Helium;
import com.stanfy.helium.internal.MethodsExecutor;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.TypeResolver;
import com.stanfy.helium.utils.AssertionUtils;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Base class for generated REST API tests.
 */
@RunWith(HeliumTest.Runner.class)
public abstract class RestApiMethods {

  /** Test specification file name. */
  public static final String TEST_SPEC_NAME = "test.spec";

  /** HTTP client instance. */
  private final OkHttpClient client = HeliumTest.httpClient();

  /** Types resolver from the specification project. */
  private TypeResolver types;

  public RestApiMethods() {
    this.types = loadDefaultTestSpec().getTypes();
  }

  public RestApiMethods(final TypeResolver types) {
    this.types = types;
  }

  protected abstract void prepareVariables(final Helium helium);

  private void prepareBaseDir(final Helium helium, final String specPath) {
    URL specUrl = getClass().getClassLoader().getResource(specPath);
    try {
      @SuppressWarnings("ConstantConditions")
      URI baseUri = specUrl.toURI();
      helium.set("baseDir", new File(baseUri).getParentFile().toURI().toString());
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  protected Project loadDefaultTestSpec() {
    String path = getSpecPath();
    InputStream input = getClass().getClassLoader().getResourceAsStream(path);
    if (input == null) {
      throw new IllegalStateException("Test spec not found in cp at " + path);
    }
    try {
      InputStreamReader source = new InputStreamReader(input, "UTF-8");
      Helium h = new Helium();
      prepareVariables(h);
      prepareBaseDir(h, path);
      return h.from(source).getProject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getSpecPath() {
    return getClass().getPackage().getName().replaceAll("\\.", "/") + "/" + TEST_SPEC_NAME;
  }

  protected MethodsExecutor createExecutor() {
    return new HttpExecutor(types, client);
  }

  protected OkHttpClient getClient() {
    return client;
  }

  /**
   * Assert either successful or client error response status code.
   * @param success true for success, false for client error
   */
  protected static void validateStatus(final Response response, final boolean success) {
    AssertionUtils.validateStatus(response, success);
  }

  /**
   * Validate obtained HTTP response body. Check whether it matches the format described in the specification.
   * @param response HTTP response instance
   * @param encoding content encoding
   * @param typeName expected response type name
   * @throws IOException in case of I/O errors
   */
  protected void validate(final Response response, final String typeName) throws IOException {
    new HttpResponseWrapper(types, response, types.byName(typeName)).getBody();
  }

}
