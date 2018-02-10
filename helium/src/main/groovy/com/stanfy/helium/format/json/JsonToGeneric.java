package com.stanfy.helium.format.json;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class JsonToGeneric {

  private JsonToGeneric() { /* hidden */ }

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


}
