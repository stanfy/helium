package com.stanfy.helium.entities.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.entities.Converter;
import com.stanfy.helium.entities.ConvertersPool;
import com.stanfy.helium.entities.MessageConverter;
import com.stanfy.helium.entities.SequenceConverter;
import com.stanfy.helium.entities.ValidationError;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides JSON converters for messages, sequences, constrained types, and default primitive types.
 */
public class JsonConvertersPool extends ConvertersPool<JsonReader, JsonWriter> {

  public static final String JSON = "json";

  @Override
  public String getFormat() {
    return JSON;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Type> Converter<T, JsonReader, JsonWriter> getConverter(final T type) {
    if (type instanceof Message) {
      return (Converter<T, JsonReader, JsonWriter>) new JsonMessageConverter((Message) type);
    }

    if (type instanceof Sequence) {
      return (Converter<T, JsonReader, JsonWriter>) new JsonSequenceConverter((Sequence)type);
    }

    return super.getConverter(type);
  }

  private final class JsonSequenceConverter extends SequenceConverter<JsonReader, JsonWriter> {

    public JsonSequenceConverter(final Sequence type) {
      super(JSON, type);
    }

    @Override
    public void write(final JsonWriter output, final Object value) throws IOException {
      output.beginArray();
      super.write(output, value);
      output.endArray();
    }

    @Override
    public ConvertersPool<JsonReader, JsonWriter> getPool() { return JsonConvertersPool.this; }

    @Override
    public List<?> read(final JsonReader input, final List<ValidationError> errors) throws IOException {
      JsonToken token = input.peek();
      if (token != JsonToken.BEGIN_ARRAY) {
        errors.add(new ValidationError(getType(), "not an array"));
        input.skipValue();
        return null;
      }
      input.beginArray();
      List<?> value = super.read(input, errors);
      input.endArray();
      return value;
    }

    @Override
    protected boolean hasNext(final JsonReader input) throws IOException {
      return input.hasNext();
    }
  }

  private final class JsonMessageConverter extends MessageConverter<JsonReader, JsonWriter> {

    public JsonMessageConverter(final Message type) {
      super(JSON, type);
    }

    @Override
    public void write(final JsonWriter output, final Object value) throws IOException {
      output.beginObject();
      super.write(output, value);
      output.endObject();
    }

    @Override
    protected void writeSequenceField(final String name, final Type itemType, final List<?> value, final JsonWriter out)
        throws IOException {
      out.name(name);
      Sequence seq = new Sequence();
      seq.setItemsType(itemType);
      new JsonSequenceConverter(seq).write(out, value);
    }

    @Override
    protected void writeField(final String name, final Type type, final Object value, final JsonWriter out)
        throws IOException {
      out.name(name);
      writeValue(type, value, out);
    }

    @Override
    public Map<String, ?> read(final JsonReader input, final List<ValidationError> errors) throws IOException {
      JsonToken token = input.peek();
      if (token != JsonToken.BEGIN_OBJECT) {
        errors.add(new ValidationError(getType(), "not an object"));
        input.skipValue();
        return null;
      }
      input.beginObject();
      Map<String, ?> value = super.read(input, errors);
      input.endObject();
      return value;
    }

    @Override
    protected String nextFieldName(final JsonReader input) throws IOException {
      return input.nextName();
    }

    @Override
    protected void skip(final JsonReader input) throws IOException {
      input.skipValue();
    }

    @Override
    protected boolean checkNextNull(final JsonReader input) throws IOException {
      return input.peek() == JsonToken.NULL;
    }

    @Override
    protected boolean hasNext(final JsonReader input) throws IOException {
      return input.hasNext();
    }

    @Override
    protected Object readSequenceField(final Field field, final JsonReader input, final List<ValidationError> errors)
        throws IOException {
      Sequence seq = new Sequence();
      seq.setItemsType(field.getType());
      return new JsonSequenceConverter(seq).read(input, errors);
    }

    @Override
    public ConvertersPool<JsonReader, JsonWriter> getPool() { return JsonConvertersPool.this; }

  }

}
