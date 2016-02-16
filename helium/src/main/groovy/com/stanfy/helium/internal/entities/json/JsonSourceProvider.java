package com.stanfy.helium.internal.entities.json;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.squareup.okhttp.MediaType;
import com.stanfy.helium.internal.entities.Converter;
import com.stanfy.helium.internal.entities.ConvertersFactory;
import com.stanfy.helium.internal.entities.EntitiesSource;
import com.stanfy.helium.internal.entities.TypedEntity;
import com.stanfy.helium.internal.entities.ValidationError;
import com.stanfy.helium.model.Type;
import okio.Okio;
import okio.Source;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * Validates whether the incoming JSON conforms the supplied message.
 */
public class JsonSourceProvider implements EntitiesSource.Factory {

  private static String getFullErrorMessage(final Throwable e) {
    StringWriter stackOut = new StringWriter();
    e.printStackTrace(new PrintWriter(stackOut));
    return stackOut.toString();
  }

  @Override
  public boolean supportsMediaType(MediaType type) {
    return "json".equals(type.subtype());
  }

  @Override
  public EntitiesSource create(final Source source, final Charset charset, final ConvertersFactory<?, ?> cFactory) {
    return new EntitiesSource() {
      @SuppressWarnings("unchecked")
      @Override
      public TypedEntity<?> read(Type type) throws IOException {
        InputStreamReader in = new InputStreamReader(Okio.buffer(source).inputStream(), charset);
        try {
          JsonReader reader = new JsonReader(in);
          reader.setLenient(true);
          ConvertersFactory factory = cFactory;
          LinkedList<ValidationError> errors = new LinkedList<ValidationError>();
          Converter<Type, JsonReader, ?> converter = factory.getConverter(type);
          Object value = null;

          try {
            value = converter.read(reader, errors);
          } catch (JsonSyntaxException e) {
            errors.add(new ValidationError(type, "Could not parse json JSON (syntax error)\n" + getFullErrorMessage(e)));
          } catch (IllegalStateException e) {
            errors.add(new ValidationError(type, "Could not parse json JSON (bad structure)\n" + getFullErrorMessage(e)));
          }

          return new TypedEntity<Type>(type, value, ValidationError.wrap(type, errors, type.isPrimitive()));
        } finally {
          in.close();
        }
      }
    };
  }

}
