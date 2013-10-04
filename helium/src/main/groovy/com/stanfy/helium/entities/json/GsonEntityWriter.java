package com.stanfy.helium.entities.json;

import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.entities.EntityWriter;
import com.stanfy.helium.entities.TypedEntity;
import com.stanfy.helium.model.*;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Writes entity as JSON object.
 */
public class GsonEntityWriter implements EntityWriter {

  /** Output. */
  private final JsonWriter out;

  /** Types. */
  private final TypeResolver types;

  public GsonEntityWriter(final Writer out, final TypeResolver types) {
    this.out = new JsonWriter(out);
    this.out.setLenient(true);
    this.types = types;
  }


  @Override
  public void write(final TypedEntity entity) throws IOException {
    write(entity.getType(), entity.getValue());
  }

  // TODO: refactor writers

  @SuppressWarnings("unchecked")
  private void write(final Type type, final Object value) throws IOException {
    if (type instanceof Message) {
      writeMessage((Message) type, (Map<String, ?>)value);
      return;
    }
    if (type instanceof Sequence) {
      writeSequence(((Sequence)type).getItemsType(), (List<?>)value);
      return;
    }
    writePrimitive(type, value);
  }

  private void writeMessage(final Message type, final Map<String, ?> values) throws IOException {
    out.beginObject();
    for (Field f : type.getFields()) {
      out.name(f.getName());
      Object value = values.get(f.getName());
      if (f.isSequence()) {
        writeSequence(f.getType(), (List<?>)value);
      } else {
        write(f.getType(), value);
      }
    }
    out.endObject();
  }

  private void writeSequence(final Type itemType, final List<?> values) throws IOException {
    out.beginArray();
    for (Object value : values) {
      write(itemType, value);
    }
    out.endArray();
  }

  private void writePrimitive(final Type type, final Object value) throws IOException {
    Class<?> clazz = types.toGroovyClass(type);
    if (Number.class.isAssignableFrom(clazz) || clazz == int.class || clazz == double.class || clazz == long.class || clazz == float.class) {
      out.value((Number)value);
    } else if (clazz == Boolean.class || clazz == boolean.class) {
      out.value((Boolean)value);
    } else {
      if (!clazz.isInstance(value)) {
        throw new IllegalArgumentException("Cannot use value of type " + value.getClass() + " as " + clazz);
      }
      out.value(value.toString());
    }
  }

}
