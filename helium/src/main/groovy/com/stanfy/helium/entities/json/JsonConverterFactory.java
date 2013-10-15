package com.stanfy.helium.entities.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.entities.*;
import com.stanfy.helium.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class JsonConverterFactory extends ConverterFactory<JsonReader, JsonWriter> {

  public static final String JSON = "json";

  @Override
  public String getFormat() {
    return JSON;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Type> Converter<T, JsonReader, JsonWriter> getConverter(final T type) {
    if (type instanceof Message) {
      return (Converter<T, JsonReader, JsonWriter>) new JsonMessageConverter((Message)type);
    }

    if (type instanceof Sequence) {
      return (Converter<T, JsonReader, JsonWriter>) new JsonSequenceConverter((Sequence)type);
    }

    return super.getConverter(type);
  }

  public abstract static class JsonPrimitiveConverter implements Converter<Type, JsonReader, JsonWriter> {

    /** Type. */
    private final Type type;

    public JsonPrimitiveConverter(final Type type) {
      this.type = type;
    }

    @Override
    public String getFormat() {
      return JSON;
    }

    @Override
    public Type getType() {
      return type;
    }

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
    public ConverterFactory<JsonReader, JsonWriter> getFactory() { return JsonConverterFactory.this; }

    @Override
    public List<?> read(JsonReader input, List<ValidationError> errors) throws IOException {
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
    protected boolean hasNext(final JsonReader input) throws IOException{
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
    protected void writeSequenceField(final String name, final Type itemType, final List<?> value, final JsonWriter out) throws IOException{
      out.name(name);
      Sequence seq = new Sequence();
      seq.setItemsType(itemType);
      new JsonSequenceConverter(seq).write(out, value);
    }

    @Override
    protected final void writeField(final String name, final Type type, final Object value, JsonWriter out) throws IOException {
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
    protected String nextFieldName(JsonReader input) throws IOException {
      return input.nextName();
    }

    @Override
    protected void skip(JsonReader input) throws IOException {
      input.skipValue();
    }

    @Override
    protected boolean checkNextNull(JsonReader input) throws IOException {
      return input.peek() == JsonToken.NULL;
    }

    @Override
    protected boolean hasNext(JsonReader input) throws IOException {
      return input.hasNext();
    }

    @Override
    protected Object readSequenceField(Field field, JsonReader input, List<ValidationError> errors) throws IOException {
      Sequence seq = new Sequence();
      seq.setItemsType(field.getType());
      return new JsonSequenceConverter(seq).read(input, errors);
    }

    @Override
    public ConverterFactory<JsonReader, JsonWriter> getFactory() { return JsonConverterFactory.this; }

  }

}
