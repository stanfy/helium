package com.stanfy.helium.internal.entities;

import com.stanfy.helium.format.FormatWriter;
import com.stanfy.helium.format.PrimitiveWriter;
import okio.Sink;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Can write entity somewhere.
 */
public interface EntitiesSink {

  void write(final TypedEntity<?> entity) throws IOException;

  /** Builder for an entity writer. */
  final class Builder extends
      SinkSourceBuilder<Sink, EntitiesSink, FormatWriter, FormatWriter.Factory, PrimitiveWriter<?>, Builder> {

    public Builder() {
      super(FormatWriter.Factory.class, EntitiesSink.class);
    }

    public Builder into(final Sink sink) {
      setTarget(sink);
      return this;
    }

    @Override
    protected EntitiesSink create(final FormatWriter format) {
      return new EntitiesSink() {
        @Override
        public void write(TypedEntity<?> entity) throws IOException {
          try {
            BaseConverter.getConverter(entity.getType()).write(format, entity.getValue());
          } finally {
            IOUtils.closeQuietly(format);
          }
        }
      };
    }
  }

}
