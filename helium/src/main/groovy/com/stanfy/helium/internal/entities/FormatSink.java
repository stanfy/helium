package com.stanfy.helium.internal.entities;

import okio.Sink;

import java.io.IOException;

/**
 * Can write entity somewhere.
 */
public interface FormatSink {

  void write(final TypedEntity<?> entity) throws IOException;

  /** Interface of a service that can provide plugable sink implementation. */
  interface Factory extends SinkSourceBuilder.SinkSourceProvider<Sink, FormatSink> { }

  /** Builder for an entity writer. */
  class Builder extends SinkSourceBuilder<Sink, FormatSink, Factory, Builder> {

    public Builder() {
      super(Factory.class, FormatSink.class);
    }

    public Builder into(final Sink sink) {
      setTarget(sink);
      return this;
    }

  }

}
