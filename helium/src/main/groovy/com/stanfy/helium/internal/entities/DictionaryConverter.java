package com.stanfy.helium.internal.entities;

import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Base class for dictionary converters. */
public abstract class DictionaryConverter<I, O> extends BaseTypeConverter<I, O> implements Converter<Dictionary, I, O> {

  private final Dictionary dictionary;

  protected DictionaryConverter(final String format, final Dictionary dictionary) {
    super(format);
    this.dictionary = dictionary;
  }

  @Override
  public final Dictionary getType() {
    return dictionary;
  }

  @Override
  public void write(O output, Object value) throws IOException {
    @SuppressWarnings("unchecked")
    Map<Object, Object> values = (Map<Object, Object>) value;

    for (Map.Entry<Object, Object> entry : values.entrySet()) {
      writeKey(output, entry.getKey(), dictionary.getKey());
      writeValue(output, entry.getValue(), dictionary.getValue());
    }
  }

  @Override
  public Object read(I input, List<ValidationError> errors) throws IOException {
    LinkedHashMap<Object, Object> map = new LinkedHashMap<Object, Object>();
    while (hasNext(input)) {
      LinkedList<ValidationError> localErrors = new LinkedList<ValidationError>();

      Object key = readKey(input, dictionary.getKey(), localErrors);
      if (!localErrors.isEmpty()) {
        errors.addAll(localErrors);
        skip(input);
        continue;
      }

      try {
        Object value = readValue(input, dictionary.getValue(), localErrors);
        if (!localErrors.isEmpty()) {
          errors.addAll(localErrors);
          skip(input);
          continue;
        }
        map.put(key, value);
      } catch (IllegalStateException e) {
        errors.add(new ValidationError(getType(), "Value of key " + key + " has bad format. " + e.getMessage()));
      }

    }
    return map;
  }

  protected abstract void writeKey(final O output, final Object value, final Type type) throws IOException;

  protected abstract void writeValue(final O output, final Object value, final Type type) throws IOException;

  protected abstract boolean hasNext(final I input) throws IOException;

  protected abstract Object readKey(final I input, final Type type, final List<ValidationError> errors) throws IOException;

  protected abstract Object readValue(final I input, final Type type, final List<ValidationError> errors) throws IOException;

  protected abstract void skip(final I input) throws IOException;

}
