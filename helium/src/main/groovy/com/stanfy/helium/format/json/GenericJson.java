package com.stanfy.helium.format.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class GenericJson {

  private GenericJson() { /* hidden */ }

  static Map<String, ?> readMap(JsonReader reader) throws IOException {
    LinkedHashMap<String, Object> result = new LinkedHashMap<>();
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      result.put(name, readValue(reader));
    }
    reader.endObject();
    return result;
  }

  static List<?> readList(JsonReader reader) throws IOException {
    ArrayList<Object> result = new ArrayList<>();
    reader.beginArray();
    while (reader.hasNext()) {
      result.add(readValue(reader));
    }
    reader.endArray();
    return result;
  }

  static Object readValue(JsonReader reader) throws IOException {
    Object value = null;
    switch (reader.peek()) {
      case BEGIN_OBJECT:
        value = readMap(reader);
        break;
      case BEGIN_ARRAY:
        value = readList(reader);
        break;
      case NUMBER:
        value = reader.nextDouble();
        break;
      case STRING:
        value = reader.nextString();
        break;
      case BOOLEAN:
        value = reader.nextBoolean();
        break;
      case NULL:
        reader.nextNull();
        break;
      default:
        reader.skipValue();
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  static void writeValue(JsonWriter output, Object value) throws IOException {
    if (value instanceof Map) {
      writeMap(output, (Map<String, ?>) value);
    } else if (value instanceof Collection) {
      writeCollection(output, (Collection<?>) value);
    } else if (value instanceof Number) {
      output.value((Number) value);
    } else if (value instanceof String) {
      output.value((String) value);
    } else if (value instanceof Boolean) {
      output.value((Boolean) value);
    } else {
      throw new IllegalArgumentException("Cannot serialize " + value + " of type " + value.getClass() + " to JSON");
    }
  }

  static void writeMap(JsonWriter output, Map<String, ?> value) throws IOException {
    output.beginObject();
    for (Map.Entry<String, ?> entry : value.entrySet()) {
      output.name(entry.getKey());
      writeValue(output, entry.getValue());
    }
    output.endObject();
  }

  static void writeCollection(JsonWriter output, Collection<?> collection) throws IOException {
    output.beginArray();
    for (Object value : collection) {
      writeValue(output, value);
    }
    output.endArray();
  }

}
