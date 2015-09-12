package com.stanfy.helium.internal.entities;

import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for message serializers.
 */
public abstract class MessageConverter<I, O> extends BaseTypeConverter<I, O> implements Converter<Message, I, O> {

  /** Type. */
  private final Message type;

  public MessageConverter(final String format, final Message type) {
    super(format);
    this.type = type;
  }

  @Override
  public Message getType() {
    return type;
  }

  @Override
  public void write(final O output, final Object value) throws IOException {
    @SuppressWarnings("unchecked")
    Map<String, Object> values = (Map<String, Object>) value;

    for (Field f : getType().getActiveFields()) {

      Object v = values.get(f.getName());
      if (f.isSequence()) {
        writeSequenceField(f.getName(), f.getType(), (List<?>) v, output);
      } else {
        if (v == null) {
          if (f.isRequired()) {
            throw new IllegalArgumentException("Field " + f.getName() + " in " + getType() + " is required. But null is provided");
          }
          continue;
        }
        writeField(f.getName(), f.getType(), v, output);
      }

    }
  }

  @Override
  public Map<String, ?> read(final I input, final List<ValidationError> errors) throws IOException {
    LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
    Set<String> visitedFields = new HashSet<String>();
    while (hasNext(input)) {
      String fieldName = nextFieldName(input);
      Field field = type.fieldByName(fieldName);

      if (field == null) {
        if (!type.isSkipUnknownFields()) {
          errors.add(new ValidationError("Unexpected field '" + fieldName + "'"));
        }
        skip(input);
        continue;
      }

      visitedFields.add(fieldName);

      Type fieldType = field.getType();
      if (fieldType instanceof Sequence) {
        throw new IllegalStateException("Sequences are accepted as roots only!");
      }

      if (field.isSkip()) {
        skip(input);
        continue;
      }

      if (checkNextNull(input)) {
        skip(input);
        if (field.isRequired()) {
          errors.add(new ValidationError(type, field, "field is required but got NULL"));
        }
        continue;
      }

      LinkedList<ValidationError> childrenErrors = new LinkedList<ValidationError>();
      if (field.isSequence()) {
        values.put(fieldName, readSequenceField(field, input, childrenErrors));
      } else {
        values.put(fieldName, readValue(fieldType, field, input, childrenErrors));
      }

      if (!childrenErrors.isEmpty()) {
        if (field.isSequence()) {
          ValidationError error = new ValidationError(type, field, "array contains errors");
          error.setChildren(childrenErrors);
          errors.add(error);
        } else if (!fieldType.isPrimitive()) {
          ValidationError error = new ValidationError(type, field, "object contains errors");
          error.setChildren(childrenErrors);
          errors.add(error);
        } else {
          errors.addAll(childrenErrors);
        }
      }

    }

    for (Field requiredField : type.getRequiredFields()) {
      if (!visitedFields.contains(requiredField.getName())) {
        errors.add(new ValidationError(type, requiredField, "field is not provided"));
      }
    }

    return values;
  }

  protected abstract boolean hasNext(final I input) throws IOException;

  protected abstract String nextFieldName(final I input) throws IOException;

  protected abstract void skip(final I input) throws IOException;

  protected abstract boolean checkNextNull(final I input) throws IOException;

  protected abstract Object readSequenceField(final Field field, final I input, final List<ValidationError> errors) throws IOException;

  protected abstract void writeSequenceField(final String name, final Type itemType, final List<?> value, final O out) throws IOException;

  protected abstract void writeField(final String name, final Type type, final Object value, final O out) throws IOException;

}
