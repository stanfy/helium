package com.stanfy.helium.internal.entities;

import com.stanfy.helium.model.Type;
import okio.Source;

import java.io.IOException;

/**
 * Can read entity from somewhere.
 */
public interface EntitiesSource {

  TypedEntity<?> read(final Type type) throws IOException;

  /** Interface of a service that can provide plugable source implementation. */
  interface Factory extends SinkSourceBuilder.SinkSourceProvider<Source, EntitiesSource> { }

  /** Builder for an entity writer. */
  class Builder extends SinkSourceBuilder<Source, EntitiesSource, Factory, Builder> {

    public Builder() {
      super(Factory.class, EntitiesSource.class);
    }

    public Builder from(final Source source) {
      setTarget(source);
      return this;
    }

  }

}
