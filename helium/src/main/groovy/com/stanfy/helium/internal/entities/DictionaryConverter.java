package com.stanfy.helium.internal.entities;

import com.stanfy.helium.format.FormatReader;
import com.stanfy.helium.format.FormatWriter;
import com.stanfy.helium.model.Dictionary;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class DictionaryConverter extends BaseConverter<Dictionary> {

  DictionaryConverter(final Dictionary dictionary) {
    super(dictionary);
  }

  @Override
  public void writeData(FormatWriter output, Object value) throws IOException {
    @SuppressWarnings("unchecked")
    Map<Object, Object> values = (Map<Object, Object>) value;
    output.beginDictionary(type);
    BaseConverter<?> valueConverter = getConverter(type.getValue());
    for (Map.Entry<Object, Object> entry : values.entrySet()) {
      output.beginDictionaryEntry(type.getKey(), entry.getKey());
      valueConverter.write(output, entry.getValue());
      output.endDictionaryEntry(type.getKey(), entry.getKey());
    }
    output.endDictionary(type);
  }

  @Override
  public Object readData(FormatReader input, List<ValidationError> errors) throws IOException {
    LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
    BaseConverter<?> valueConverter = getConverter(type.getValue());
    input.beginDictionary(type);
    while (input.hasNext()) {
      LinkedList<ValidationError> localErrors = new LinkedList<>();

      Object key;
      try {
        key = input.nextDictionaryKey(type, type.getKey());
      } catch (IllegalArgumentException | IllegalStateException e) {
        input.skipValue();
        errors.add(new ValidationError(type, e.getMessage()));
        return null;
      }

      try {
        Object value = valueConverter.read(input, localErrors);
        if (!localErrors.isEmpty()) {
          ValidationError error = new ValidationError(type.getValue(), "Value for key " + key + " got errors");
          error.setChildren(localErrors);
          errors.add(error);
          continue;
        }
        map.put(key, value);
      } catch (IllegalStateException e) {
        errors.add(new ValidationError(getType(), "Value of key " + key + " has bad format. " + e.getMessage()));
      }

    }
    input.endDictionary(type);
    return map;
  }

}
