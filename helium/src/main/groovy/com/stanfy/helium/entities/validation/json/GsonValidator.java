package com.stanfy.helium.entities.validation.json;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.stanfy.helium.entities.validation.ValidationError;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.*;
import java.util.*;

/**
 * JSON validator that uses JsonReader from Google GSON.
 */
public class GsonValidator extends JsonValidator {

  public GsonValidator(Type type) {
    super(type);
  }

  public List<ValidationError> validate(final Reader reader) {
    try {
      JsonReader json = new JsonReader(reader);
      json.setLenient(true);
      return validate(json);
    } catch (JsonSyntaxException e) {
      return Collections.singletonList(new ValidationError(getType(), "Could not parse input JSON (syntax error)\n" + getFullErrorMessage(e)));
    } catch (IllegalStateException e) {
      return Collections.singletonList(new ValidationError(getType(), "Could not parse input JSON (bad response structure)\n" + getFullErrorMessage(e)));
    } catch (IOException e) {
      return Collections.singletonList(new ValidationError(getType(), "Could not parse input JSON (I/O error)\n" + getFullErrorMessage(e)));
    }
  }

  List<ValidationError> validate(final JsonReader json) throws IOException {
    LinkedList<ValidationError> errors = new LinkedList<ValidationError>();
    Type type = getType();
    boolean object = type instanceof Message;
    boolean array = type instanceof Sequence;

    if (!object && !array) {
      Field f = new Field();
      f.setName("unknown");
      f.setType(type);
      validatePrimitive(f, json, errors);
      return errors;
    }

    switch (json.peek()) {
      case BEGIN_OBJECT:
        if (array) {
          errors.add(new ValidationError(type, "input is not an array"));
          json.skipValue();
          return errors;
        }
        json.beginObject();
        break;

      case BEGIN_ARRAY:
        if (!array) {
          errors.add(new ValidationError(type, "input is not an object"));
          json.skipValue();
          return errors;
        }
        json.beginArray();
        break;

      default:
        errors.add(new ValidationError(type, "unexpected token " + json.peek()));
        json.skipValue();
        return errors;
    }

    if (object) {
      validateObjectFields((Message)type, json, errors);
      json.endObject();
    }

    if (array) {
      validateArrayValue(((Sequence)type).getItemsType(), json, errors);
      json.endArray();
    }

    return errors;
  }

  private void validateArrayValue(final Type itemType, final JsonReader json, final List<ValidationError> errors) throws IOException {
    GsonValidator validator = new GsonValidator(itemType);
    int index = 0;
    while (json.hasNext()) {
      List<ValidationError> children = validator.validate(json);
      if (!children.isEmpty()) {
        ValidationError error = new ValidationError(itemType, "Item " + index + " contains errors");
        error.setChildren(children);
        errors.add(error);
      }
      index++;
    }
  }

  private void validateObjectFields(final Message message, final JsonReader json, final List<ValidationError> errors) throws IOException {
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

      if (field.isSequence()) {

        json.beginArray();
        LinkedList<ValidationError> children = new LinkedList<ValidationError>();
        validateArrayValue(fieldType, json, children);
        if (!children.isEmpty()) {
          ValidationError fieldError = new ValidationError(message, field, "there are some errors inside");
          fieldError.setChildren(children);
          errors.add(fieldError);
        }
        json.endArray();

      } else if (fieldType instanceof Message) {
        GsonValidator nextValidator = new GsonValidator(fieldType);
        List<ValidationError> children = nextValidator.validate(json);
        if (!children.isEmpty()) {
          ValidationError error = new ValidationError(message, field, "there are some errors inside");
          error.setChildren(children);
          errors.add(error);
        }
      } else {
        // primitive type
        validatePrimitive(field, json, errors);
      }

    }

    for (Field requiredField : message.getRequiredFields()) {
      if (!visitedFields.contains(requiredField.getName())) {
        errors.add(new ValidationError(message, requiredField, "field is not provided"));
      }
    }
  }

  private void validatePrimitive(final Field field, final JsonReader json, final List<ValidationError> errors) throws IOException {
    String explanation = new DefaultJsonTypeValidator().validateNextValue(new DefaultGsonValuePuller(json), field.getType());
    if (explanation == null) {
      return;
    }
    errors.add(new ValidationError(getType(), field, explanation));
  }

}
