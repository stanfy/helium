package com.stanfy.helium.internal.entities;

import com.google.gson.JsonSyntaxException;
import com.stanfy.helium.format.FormatReader;
import com.stanfy.helium.format.PrimitiveReader;
import com.stanfy.helium.model.Type;
import okio.Source;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;

/**
 * Can read entity from somewhere.
 */
public interface EntitiesSource {

  TypedEntity<?> read(final Type type) throws IOException;

  /** Builder for an entity writer. */
  class Builder extends
      SinkSourceBuilder<Source, EntitiesSource, FormatReader, FormatReader.Factory, PrimitiveReader<?>, Builder> {

    public Builder() {
      super(FormatReader.Factory.class, EntitiesSource.class);
    }

    public Builder from(final Source source) {
      setTarget(source);
      return this;
    }

    private static String getFullErrorMessage(final Throwable e) {
      StringWriter stackOut = new StringWriter();
      e.printStackTrace(new PrintWriter(stackOut));
      return stackOut.toString();
    }

    @Override
    protected EntitiesSource create(final FormatReader format) {
      return new EntitiesSource() {
        @Override
        public TypedEntity<?> read(Type type) throws IOException {
          LinkedList<ValidationError> errors = new LinkedList<>();
          Object value = null;
          try {
            value = BaseConverter.getConverter(type).read(format, errors);
          } catch (JsonSyntaxException e) {
            errors.add(new ValidationError(type,
                "Could not parse json JSON (syntax error)\n" + getFullErrorMessage(e)));
          } catch (IllegalStateException e) {
            errors.add(new ValidationError(type,
                "Could not parse json JSON (bad structure)\n" + getFullErrorMessage(e)));
          } finally {
            IOUtils.closeQuietly(format);
          }
          return new TypedEntity<>(type, value, ValidationError.wrap(type, errors, type.isPrimitive()));
        }
      };
    }
  }

}
