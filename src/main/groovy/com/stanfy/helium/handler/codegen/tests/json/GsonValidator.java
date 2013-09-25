package com.stanfy.helium.handler.codegen.tests.json;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.stanfy.helium.handler.validation.ValidationError;
import com.stanfy.helium.handler.validation.json.DefaultJsonTypeValidator;
import com.stanfy.helium.handler.validation.json.JsonValidator;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Type;

import java.io.*;
import java.util.*;

/**
 * JSON validator that uses JsonReader from Google GSON.
 */
public class GsonValidator extends JsonValidator {

  public GsonValidator(Message message) {
    super(message);
  }

  public List<ValidationError> validate(final Reader reader) throws IOException {
    try {
      return validate(new JsonReader(reader));
    } catch (JsonSyntaxException e) {
      return Collections.singletonList(new ValidationError(getMessage(), "Could not parse input JSON\n" + getFullErrorMessage(e)));
    } catch (IllegalStateException e) {
      return Collections.singletonList(new ValidationError(getMessage(), "Could not parse input JSON\n" + getFullErrorMessage(e)));
    }
  }

  List<ValidationError> validate(final JsonReader json) throws IOException {
    LinkedList<ValidationError> errors = new LinkedList<ValidationError>();
    Message message = getMessage();

    boolean array = message.isArray();
    switch (json.peek()) {
      case BEGIN_OBJECT:
        if (array) {
          errors.add(new ValidationError(message, "input is not an array"));
          json.skipValue();
          return errors;
        }
        json.beginObject();
        break;

      case BEGIN_ARRAY:
        if (!array) {
          errors.add(new ValidationError(message, "input is not an object"));
          json.skipValue();
          return errors;
        }
        json.beginArray();
        break;

      default:
        errors.add(new ValidationError(message, "unexpected token " + json.peek()));
        json.skipValue();
        return errors;
    }

    if (!array) {
      validateObjectFields(json, errors);
    }

    if (array) {
      json.endArray();
    } else {
      json.endObject();
    }
    return errors;
  }

  private void validateObjectFields(final JsonReader json, final List<ValidationError> errors) throws IOException {
    Message message = getMessage();
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
      if (fieldType instanceof Message) {
        GsonValidator nextValidator = new GsonValidator((Message)fieldType);
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

  private void validatePrimitive(final Field field, final JsonReader json, final List<ValidationError> errors) throws IOException{
    String explanation = new DefaultJsonTypeValidator().validateNextValue(new DefaultGsonValuePuller(json), field.getType());
    if (explanation == null) {
      return;
    }
    errors.add(new ValidationError(getMessage(), field, explanation));
  }

}
