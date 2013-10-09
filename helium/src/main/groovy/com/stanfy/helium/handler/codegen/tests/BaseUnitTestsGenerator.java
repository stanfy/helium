package com.stanfy.helium.handler.codegen.tests;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.model.MethodType;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Service;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.fest.assertions.api.Assertions;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.*;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Base generator.
 */
abstract class BaseUnitTestsGenerator implements Handler {

  /** Encoding constant string. */
  protected static final String UTF_8 = "UTF-8";

  /** Default package name. */
  protected static final String DEFAULT_PACKAGE_NAME = "spec.tests.rest";

  /** Public method. */
  protected static final Set<Modifier> PUBLIC = Collections.singleton(Modifier.PUBLIC);
  /** Protected method. */
  protected static final Set<Modifier> PROTECTED = Collections.singleton(Modifier.PROTECTED);

  private static final String IMPORT_TEST = "org.junit.Test",
                              IMPORT_HTTP_METHODS = "org.apache.http.client.methods.*";

  /** Output directory. */
  private final File srcOutput;

  /** Resources output. */
  private final File resourcesOutput;

  /** Package name for tests. */
  private final String packageName;

  public BaseUnitTestsGenerator(final File srcOutput, final File resourcesOutput, final String packageName) {
    checkDirectory(srcOutput, "Sources output");
    if (resourcesOutput != null) {
      checkDirectory(resourcesOutput, "Resources output");
    }

    this.srcOutput = srcOutput;
    this.resourcesOutput = resourcesOutput == null ? srcOutput : resourcesOutput;
    this.packageName = packageName == null ? DEFAULT_PACKAGE_NAME : packageName;
  }

  private static void checkDirectory(final File dir, final String name) {
    if (dir == null) { throw new IllegalArgumentException(name + " is not defined"); }
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new IllegalArgumentException(name + " does not exist and cannot be created");
      }
    } else if (!dir.isDirectory()) {
      throw new IllegalArgumentException(name + " is not a directory");
    }
  }

  public File getSrcOutput() {
    return srcOutput;
  }

  public File getResourcesOutput() {
    return resourcesOutput;
  }

  public String getPackageName() {
    return packageName;
  }

  private File withPackage(final File dir) {
    File result = new File(dir, packageName.replaceAll("\\.", "/"));
    if (!result.mkdirs() && !result.exists()) {
      throw new IllegalStateException("Cannot create dir " + result);
    }
    return result;
  }

  public File getSourcesPackageDir() { return withPackage(getSrcOutput()); }

  public File getResourcesPackageDir() { return withPackage(getResourcesOutput()); }

  File getSpecFile() { return new File(getResourcesPackageDir(), RestApiMethods.TEST_SPEC_NAME); }

  protected void startTest(final JavaWriter java, final Service service) throws IOException {
    java.emitPackage(getPackageName())
        .emitImports(IMPORT_HTTP_METHODS)
        .emitImports(
            Test.class.getName(),
            MethodType.class.getName(), RestApiMethods.class.getName(), URI.class.getName(),
            HttpResponse.class.getName(), HttpEntity.class.getName(), StringEntity.class.getName(), HttpEntityEnclosingRequestBase.class.getName()
        )
        .emitStaticImports(Assertions.class.getName() + ".assertThat")
        .beginType(getClassName(service), "class", PUBLIC, RestApiMethods.class.getSimpleName());
  }

  protected File getTestFile(final String className) {
    return new File(getSourcesPackageDir(), className + ".java");
  }

  private JavaWriter createTestsWriter(final String className) {
    File dst = getTestFile(className);
    try {
      OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dst), UTF_8);
      return new JavaWriter(out);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract String getClassName(final Service service);

  protected void eachService(final Project project, final ServiceHandler handler) throws IOException {
    for (Service service : project.getServices()) {
      JavaWriter writer = createTestsWriter(getClassName(service));
      try {
        startTest(writer, service);
        handler.process(service, writer);
        writer.endType();
      } finally {
        writer.close();
      }
    }
  }

  public interface ServiceHandler {
    void process(final Service service, final JavaWriter writer);
  }

}
