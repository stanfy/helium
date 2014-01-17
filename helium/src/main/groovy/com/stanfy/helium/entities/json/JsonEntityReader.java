package com.stanfy.helium.entities.json;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.stanfy.helium.entities.Converter;
import com.stanfy.helium.entities.ConverterFactory;
import com.stanfy.helium.entities.EntityReader;
import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.entities.ValidationError;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.LinkedList;

/**
 * Validates whether the incoming JSON conforms the supplied message.
 */
public class JsonEntityReader implements EntityReader {

  /** JSON json. */
  private final JsonReader json;

  /** Converters. */
  private final ConverterFactory<JsonReader, ?> converters;

  public JsonEntityReader(final Reader reader, final ConverterFactory<JsonReader, ?> converters) {
    this.json = new JsonReader(reader);
    this.json.setLenient(true);
    this.converters = converters;
  }

  private static String getFullErrorMessage(final Throwable e) {
    StringWriter stackOut = new StringWriter();
    e.printStackTrace(new PrintWriter(stackOut));
    return stackOut.toString();
  }

  @Override
  public TypedEntity read(final Type type) throws IOException {
    LinkedList<ValidationError> errors = new LinkedList<ValidationError>();
    Converter<Type, JsonReader, ?> converter = converters.getConverter(type);
    Object value = null;

    try {
      value = converter.read(json, errors);
    } catch (JsonSyntaxException e) {
      errors.add(new ValidationError(type, "Could not parse json JSON (syntax error)\n" + getFullErrorMessage(e)));
    } catch (IllegalStateException e) {
      errors.add(new ValidationError(type, "Could not parse json JSON (bad response structure)\n" + getFullErrorMessage(e)));
    }

    TypedEntity<?> res = new TypedEntity<Type>(type, value);
    res.setValidationError(ValidationError.wrap(type, errors, type.isPrimitive()));
    return res;
  }

}
