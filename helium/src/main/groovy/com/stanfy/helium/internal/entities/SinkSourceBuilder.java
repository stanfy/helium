package com.stanfy.helium.internal.entities;

import com.squareup.okhttp.MediaType;
import com.stanfy.helium.format.BaseFormatReader;
import com.stanfy.helium.format.BaseFormatWriter;
import com.stanfy.helium.format.Format;
import com.stanfy.helium.format.PrimitiveReader;
import com.stanfy.helium.format.PrimitiveWriter;
import com.stanfy.helium.model.Type;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static com.stanfy.helium.internal.utils.AssertionUtils.notNull;

/**
 * Base class for builders of FormatSink and FormatSource.
 */
abstract class SinkSourceBuilder<Target, R, FR extends Format, F extends Format.FormatProvider<Target, FR>,
    A, B extends SinkSourceBuilder<Target, R, FR, F, A, B>> {

  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private final B self;
  private final Class<F> providerClass;
  private final Class<R> resultClass;

  private Target target;
  private MediaType mediaType;
  private Charset charset;
  @SuppressWarnings("unchecked")
  private Map<Type, A> customFormatAdapters = new HashMap();

  private Class<? extends F> providerImplClass;

  @SuppressWarnings("unchecked")
  SinkSourceBuilder(final Class<F> providerClass, final Class<R> resultClass) {
    this.self = (B) this;
    this.providerClass = providerClass;
    this.resultClass = resultClass;
  }

  private F resolveProvider() {
    if (providerImplClass != null) {
      try {
        return providerImplClass.newInstance();
      } catch (Exception e) {
        throw new IllegalArgumentException("Cannot instantiate provider " + providerImplClass, e);
      }
    }

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

  public B provider(final Class<? extends F> providerImplClass) {
    notNull("'provider'", providerImplClass);
    this.providerImplClass = providerImplClass;
    return self;
  }

  public B customAdapters(final Map<Type, A> adapters) {
    notNull("'adapters'", adapters);
    this.customFormatAdapters.putAll(adapters);
    return self;
  }

  public B customAdapter(final Type type, final A adapter) {
    notNull("'type'", type);
    notNull("'adapter'", adapter);
    if (customFormatAdapters.containsKey(type)) {
      throw new IllegalStateException("Custom adapter for " + type + " is already defined ("
          + customFormatAdapters.get(type) + "). Cannot add adapter " + adapter);
    }
    customFormatAdapters.put(type, adapter);
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
    FR format = resolveProvider().create(target, charset);
    registerAdapters(format);
    return create(format);
  }

  protected abstract R create(FR format);

  @SuppressWarnings("unchecked")
  private void registerAdapters(FR format) {
    if (!customFormatAdapters.isEmpty()) {
      // TODO: Can we use BaseFormat?
      if (format instanceof BaseFormatReader<?>) {
        BaseFormatReader<?> f = (BaseFormatReader<?>) format;
        for (Map.Entry<Type, A> entry : customFormatAdapters.entrySet()) {
          f.registerPrimitiveAdapter(entry.getKey(), (PrimitiveReader) entry.getValue());
        }
      } else if (format instanceof BaseFormatWriter<?>) {
        BaseFormatWriter<?> f = (BaseFormatWriter<?>) format;
        for (Map.Entry<Type, A> entry : customFormatAdapters.entrySet()) {
          f.registerPrimitiveAdapter(entry.getKey(), (PrimitiveWriter) entry.getValue());
        }
      }
    }
  }

}
