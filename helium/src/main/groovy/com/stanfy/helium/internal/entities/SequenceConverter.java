package com.stanfy.helium.internal.entities;

import com.stanfy.helium.format.FormatReader;
import com.stanfy.helium.format.FormatWriter;
import com.stanfy.helium.model.Sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Sequence serializer.
 */
final class SequenceConverter extends BaseConverter<Sequence> {

  public SequenceConverter(final Sequence type) {
    super(type);
  }

  @Override
  public void writeData(final FormatWriter output, final Object value) throws IOException {
    List<?> list = (List<?>) value;
    output.beginSequence(type);
    BaseConverter<?> valueConverter = getConverter(type.getItemsType());
    for (Object v : list) {
      valueConverter.write(output, v);
    }
    output.endSequence(type);
  }

  @Override
  public List<?> readData(final FormatReader input, final List<ValidationError> errors) throws IOException {
    ArrayList<Object> result = new ArrayList<Object>();
    int index = 0;
    BaseConverter<?> valuesConverter = getConverter(type.getItemsType());
    input.beginSequence(type);
    while (input.hasNext()) {
      LinkedList<ValidationError> children = new LinkedList<ValidationError>();
      result.add(valuesConverter.read(input, children));

      if (!children.isEmpty()) {
        ValidationError error = new ValidationError(type.getItemsType(), index, "item contains errors");
        error.setChildren(children);
        errors.add(error);
      }

      index++;
    }
    input.endSequence(type);
    return result;
  }

}
