package com.stanfy.helium.handler.tests.body;

import com.stanfy.helium.handler.tests.RequestBodyBuilder;
import com.stanfy.helium.model.Type;

import java.util.HashSet;

/**
 * @author Nikolay Soroka (Stanfy - http://www.stanfy.com)
 */
public final class BuilderFactory {

  /**
   * Set of builder that are used by
   * {@link com.stanfy.helium.handler.tests.HttpExecutor#performMethod(com.stanfy.helium.model.Service, com.stanfy.helium.model.ServiceMethod,
   * com.stanfy.helium.internal.ServiceMethodRequestValues)}.
   */
  private static HashSet<RequestBodyBuilder> sBuilders = new HashSet<RequestBodyBuilder>();

  private static RequestBodyBuilder defaultBodyBuilder = new JsonConverterBuilder();

  private BuilderFactory() {
    /* no instance */
  }

  /**
   * Registers {@link RequestBodyBuilder} that
   * are used in {@link com.stanfy.helium.handler.tests.HttpExecutor#performMethod(com.stanfy.helium.model.Service,
   *  com.stanfy.helium.model.ServiceMethod,
   *  com.stanfy.helium.internal.ServiceMethodRequestValues)}
   * @param builder new builder to register
   */
  static void register(final RequestBodyBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Builder may not be null.");
    }
    sBuilders.add(builder);
  }

  static {
    register(new BytesBodyBuilder());
    register(new FormBodyBuilder());
    register(new MultipartBodyBuilder());
  }

  /**
   * Returns new body builder for given type, or default one
   * if not found.
   * @param bodyType type to convert
   * @return applicable {@link RequestBodyBuilder}
   *  or default one.
   */
  public static RequestBodyBuilder getBuilderFor(final Type bodyType) {
    for (RequestBodyBuilder builder : sBuilders) {
      if (builder.canBuild(bodyType)) {
        return builder;
      }
    }
    return defaultBodyBuilder;
  }
}
