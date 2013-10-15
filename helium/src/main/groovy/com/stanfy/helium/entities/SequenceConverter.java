package com.stanfy.helium.entities;

import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Sequence serializer.
 */
public abstract class SequenceConverter<I, O> extends BaseTypeConverter<I, O> implements Converter<Sequence, I, O> {

  /** Type. */
  private final Sequence type;

  public SequenceConverter(String format, Sequence type) {
    super(format);
    this.type = type;
  }

  @Override
  public Sequence getType() {
    return type;
  }

  @Override
  public void write(O output, Object value) throws IOException {
    List<?> list = (List<?>) value;
    for (Object v : list) {
      writeValue(getType().getItemsType(), v, output);
    }
  }

  @Override
  public List<?> read(I input, List<ValidationError> errors) throws IOException {
    ArrayList<Object> result = new ArrayList<Object>();
    int index = 0;
    while (hasNext(input)) {
      LinkedList<ValidationError> children = new LinkedList<ValidationError>();
      result.add(readValue(type.getItemsType(), input, children));

      if (!children.isEmpty()) {
        ValidationError error = new ValidationError(type.getItemsType(), "Item " + index + " contains errors");
        error.setChildren(children);
        errors.add(error);
      }

      index++;
    }
    return result;
  }

  protected abstract boolean hasNext(I input) throws IOException;

}
