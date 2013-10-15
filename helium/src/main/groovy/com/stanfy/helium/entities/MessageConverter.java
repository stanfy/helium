package com.stanfy.helium.entities;

import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.*;

/**
 * Base class for message serializers.
 */
public abstract class MessageConverter<I, O> extends BaseTypeConverter<I, O> implements Converter<Message, I, O> {

  /** Type. */
  private final Message type;

  public MessageConverter(String format, Message type) {
    super(format);
    this.type = type;
  }

  @Override
  public Message getType() { return type; }

  @Override
  public void write(final O output, final Object value) throws IOException {
    @SuppressWarnings("unchecked")
    Map<String, Object> values = (Map<String, Object>) value;

    for (Field f : getType().getFields()) {
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
  public Map<String, ?> read(I input, List<ValidationError> errors) throws IOException {
    LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
    Set<String> visitedFields = new HashSet<String>();
    while (hasNext(input)) {
      String fieldName = nextFieldName(input);
      Field field = type.fieldByName(fieldName);

      if (field == null) {
        errors.add(new ValidationError(type, "Unexpected field " + fieldName));
        skip(input);
        continue;
      }

      visitedFields.add(fieldName);

      Type fieldType = field.getType();
      if (fieldType instanceof Sequence) {
        throw new IllegalStateException("Sequences are accepted as roots only!");
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
        // primitive type
        values.put(fieldName, readValue(fieldType, input, childrenErrors));
      }

      if (!childrenErrors.isEmpty()) {
        ValidationError fieldError = new ValidationError(type, field, "there are some errors inside");
        fieldError.setChildren(childrenErrors);
        errors.add(fieldError);
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
