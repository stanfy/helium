package com.stanfy.helium.handler.codegen.tests;

import com.squareup.okhttp.OkHttpClient;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Helium test tools.
 */
public final class HeliumTest {

  /** Provides HTTP client used in tests. */
  public static OkHttpClient httpClient() {
    // TODO: Use one client.
    return new OkHttpClient();
  }

  /** Helium tests runner. */
  public static class Runner extends BlockJUnit4ClassRunner {

    public Runner(Class<?> klass) throws InitializationError {
      super(klass);
    }
  }

}
