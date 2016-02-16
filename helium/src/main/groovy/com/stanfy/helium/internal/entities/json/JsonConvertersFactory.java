package com.stanfy.helium.internal.entities.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.DefaultType;
import com.stanfy.helium.internal.entities.Converter;
import com.stanfy.helium.internal.entities.ConvertersFactory;
import com.stanfy.helium.internal.entities.DictionaryConverter;
import com.stanfy.helium.internal.entities.MessageConverter;
import com.stanfy.helium.internal.entities.SequenceConverter;
import com.stanfy.helium.internal.entities.ValidationError;
import com.stanfy.helium.model.Dictionary;
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
public class JsonConvertersFactory extends ConvertersFactory<JsonReader, JsonWriter> {

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

    if (type instanceof Dictionary) {
      return (Converter<T, JsonReader, JsonWriter>) new JsonDictionaryConverter((Dictionary) type);
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
    public ConvertersFactory<JsonReader, JsonWriter> getFactory() {
      return JsonConvertersFactory.this;
    }

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
      if (value == null) {
        return;
      }
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
    public ConvertersFactory<JsonReader, JsonWriter> getFactory() {
      return JsonConvertersFactory.this;
    }

  }

  private final class JsonDictionaryConverter extends DictionaryConverter<JsonReader, JsonWriter> {

    JsonDictionaryConverter(Dictionary dictionary) {
      super(JSON, dictionary);
    }

    @Override
    public void write(JsonWriter output, Object value) throws IOException {
      output.beginObject();
      super.write(output, value);
      output.endObject();
    }

    @Override
    protected void writeKey(JsonWriter output, Object value, Type type) throws IOException {
      output.name(String.valueOf(value));
    }

    @Override
    protected void writeValue(JsonWriter output, Object value, Type type) throws IOException {
      getConverter(type).write(output, value);
    }

    @Override
    protected boolean hasNext(JsonReader input) throws IOException {
      return input.hasNext();
    }

    @Override
    protected Object readKey(JsonReader input, Type type, List<ValidationError> errors) throws IOException {
      String name = input.nextName();
      if (!type.isPrimitive() || !type.getName().equals(DefaultType.STRING.getLangName())) {
        errors.add(new ValidationError("Dictionary key can be a string only for JSON. It's a spec problem - got "
            + type));
      }
      return name;
    }

    @Override
    protected Object readValue(JsonReader input, Type type, List<ValidationError> errors) throws IOException {
      return getConverter(type).read(input, errors);
    }

    @Override
    protected void skip(JsonReader input) throws IOException {
      input.skipValue();
    }

    @Override
    public ConvertersFactory<JsonReader, JsonWriter> getFactory() {
      return JsonConvertersFactory.this;
    }

    @Override
    public Object read(JsonReader input, List<ValidationError> errors) throws IOException {
      input.beginObject();
      Object result = super.read(input, errors);
      input.endObject();
      return result;
    }
  }

}
