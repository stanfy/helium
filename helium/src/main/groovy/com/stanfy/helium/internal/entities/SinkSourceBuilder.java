package com.stanfy.helium.internal.entities;

import com.squareup.okhttp.MediaType;
import com.stanfy.helium.model.TypeResolver;

import java.nio.charset.Charset;
import java.util.ServiceLoader;

import static com.stanfy.helium.internal.utils.AssertionUtils.notNull;

/**
 * Base class for builders of FormatSink and FormatSource.
 */
abstract class SinkSourceBuilder<Target, R, F extends SinkSourceBuilder.SinkSourceProvider<Target, R>,
    B extends SinkSourceBuilder<Target, R, F, B>> {

  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private final B self;
  private final Class<F> providerClass;
  private final Class<R> resultClass;

  private Target target;
  private MediaType mediaType;
  private Charset charset;
  private TypeResolver types;

  @SuppressWarnings("unchecked")
  SinkSourceBuilder(final Class<F> providerClass, final Class<R> resultClass) {
    this.self = (B) this;
    this.providerClass = providerClass;
    this.resultClass = resultClass;
  }

  private F resolveProvider() {
    for (F provider: ServiceLoader.load(providerClass)) {
      if (provider.supportsMediaType(mediaType)) {
        return provider;
      }
    }
    throw new IllegalStateException("Cannot find "
        + resultClass.getSimpleName()
        + " provider for media type " + mediaType);
  }

  protected void setTarget(final Target target) {
    notNull("'target'", target);
    this.target = target;
  }

  public B mediaType(final MediaType mediaType) {
    notNull("'mediaType'", mediaType);
    this.mediaType = mediaType;
    return self;
  }

  public B charset(final Charset charset) {
    notNull("'charset'", charset);
    this.charset = charset;
    return self;
  }

  public B charset(final String charset) {
    notNull("'charset'", charset);
    this.charset = Charset.forName(charset);
    return self;
  }

  public B types(final TypeResolver types) {
    notNull("'types'", types);
    this.types = types;
    return self;
  }

  public R build() {
    if (target == null) {
      throw new IllegalStateException("Target is not defined");
    }
    if (mediaType == null) {
      throw new IllegalStateException("Media type is not defined");
    }
    if (charset == null) {
      charset = mediaType.charset(UTF_8);
    }
    ConvertersFactory<?, ?> converters = types.findConverters(mediaType);
    return resolveProvider()
        .create(target, charset, converters);
  }

  /** Base factory interface for FormatSink and FormatSource. */
  interface SinkSourceProvider<Target, R> {

    boolean supportsMediaType(MediaType type);

    R create(Target target, Charset charset, ConvertersFactory<?, ?> cFactory);

  }

}
