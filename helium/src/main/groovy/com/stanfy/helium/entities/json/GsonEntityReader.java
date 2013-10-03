package com.stanfy.helium.entities.json;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.stanfy.helium.entities.EntityReader;
import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.entities.json.GsonValuePuller;
import com.stanfy.helium.entities.ValidationError;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.*;

/**
 * Validates whether the incoming JSON conforms the supplied message.
 */
public class GsonEntityReader implements EntityReader {

  /** JSON json. */
  private final JsonReader json;

  public GsonEntityReader(final Reader reader) {
    this.json = new JsonReader(reader);
    this.json.setLenient(true);
  }

  private static String getFullErrorMessage(final Throwable e) {
    StringWriter stackOut = new StringWriter();
    e.printStackTrace(new PrintWriter(stackOut));
    return stackOut.toString();
  }

  @Override
  public TypedEntity read(final Type type) throws IOException {
    LinkedList<ValidationError> errors = new LinkedList<ValidationError>();
    Object value = null;

    try {
      value = read(type, errors);
    } catch (JsonSyntaxException e) {
      errors.add(new ValidationError(type, "Could not parse json JSON (syntax error)\n" + getFullErrorMessage(e)));
    } catch (IllegalStateException e) {
      errors.add(new ValidationError(type, "Could not parse json JSON (bad response structure)\n" + getFullErrorMessage(e)));
    }

    TypedEntity res = new TypedEntity(type, value);
    res.setValidationErrors(errors);
    return res;
  }

  private Object read(final Type type, final List<ValidationError> errors) throws IOException {
    boolean object = type instanceof Message;
    boolean array = type instanceof Sequence;

    if (!object && !array) {
      Field f = new Field();
      f.setName("_unknown_");
      f.setType(type);
      return readPrimitive(type, f, errors);
    }

    switch (json.peek()) {
      case BEGIN_OBJECT:
        if (array) {
          errors.add(new ValidationError(type, "json is not an array"));
          json.skipValue();
          return null;
        }
        json.beginObject();
        break;

      case BEGIN_ARRAY:
        if (!array) {
          errors.add(new ValidationError(type, "json is not an object"));
          json.skipValue();
          return null;
        }
        json.beginArray();
        break;

      default:
        errors.add(new ValidationError(type, "unexpected token " + json.peek()));
        json.skipValue();
        return null;
    }

    Object value = null;

    if (object) {
      value = readObjectFields((Message) type, errors);
      json.endObject();
    }

    if (array) {
      value = readArrayValue(((Sequence) type).getItemsType(), errors);
      json.endArray();
    }

    return value;
  }

  private Object readArrayValue(final Type itemType, final List<ValidationError> errors) throws IOException {
    ArrayList<Object> value = new ArrayList<Object>();
    int index = 0;
    while (json.hasNext()) {
      LinkedList<ValidationError> children = new LinkedList<ValidationError>();
      value.add(read(itemType, children));

      if (!children.isEmpty()) {
        ValidationError error = new ValidationError(itemType, "Item " + index + " contains errors");
        error.setChildren(children);
        errors.add(error);
      }

      index++;
    }
    return value;
  }

  private Object readObjectFields(final Message message, final List<ValidationError> errors) throws IOException {
    LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
    Set<String> visitedFields = new HashSet<String>();
    while (json.hasNext()) {
      String fieldName = json.nextName();
      Field field = message.fieldByName(fieldName);

      if (field == null) {
        errors.add(new ValidationError(message, "Unexpected field " + fieldName));
        json.skipValue();
        continue;
      }

      visitedFields.add(fieldName);

      Type fieldType = field.getType();
      if (fieldType instanceof Sequence) {
        throw new IllegalStateException("Sequences are accepted as roots only!");
      }

      LinkedList<ValidationError> childrenErrors = new LinkedList<ValidationError>();
      if (field.isSequence()) {

        json.beginArray();
        values.put(fieldName, readArrayValue(fieldType, childrenErrors));
        json.endArray();

      } else if (fieldType instanceof Message) {

        json.beginObject();
        values.put(fieldName, readObjectFields((Message)fieldType, childrenErrors));
        json.endObject();

      } else {
        // primitive type
        values.put(fieldName, readPrimitive(message, field, errors));
      }

      if (!childrenErrors.isEmpty()) {
        ValidationError fieldError = new ValidationError(message, field, "there are some errors inside");
        fieldError.setChildren(childrenErrors);
        errors.add(fieldError);
      }

    }

    for (Field requiredField : message.getRequiredFields()) {
      if (!visitedFields.contains(requiredField.getName())) {
        errors.add(new ValidationError(message, requiredField, "field is not provided"));
      }
    }

    return values;
  }

  private Object readPrimitive(final Type parentType, final Field field, final List<ValidationError> errors) throws IOException {
    try {
      return new DefaultJsonTypeValidator().validateNextValue(new GsonValuePuller(json), field.getType(), field.isRequired());
    } catch (IllegalStateException e) {
      String explanation = e.getMessage();
      errors.add(new ValidationError(parentType, field, explanation));
      return null;
    }
  }
}
