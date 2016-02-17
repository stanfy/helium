package com.stanfy.helium.format.json;

import com.google.gson.stream.JsonWriter;
import com.stanfy.helium.DefaultType;
import com.stanfy.helium.format.BaseFormatWriter;
import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.IOException;

final class JsonWriteFormat extends BaseFormatWriter<JsonWriter> {

  JsonWriteFormat(JsonWriter writer) {
    super(writer);
  }

  @Override
  public void beginMessage(Message message) throws IOException {
    getOutput().beginObject();
  }

  @Override
  public void endMessage(Message message) throws IOException {
    getOutput().endObject();
  }

  @Override
  public void beginMessageField(Field field) throws IOException {
    getOutput().name(field.getName());
  }

  @Override
  public void endMessageField(Field field) {
    // Nothing.
  }

  @Override
  public void beginDictionaryEntry(Type type, Object key) throws IOException {
    if (!DefaultType.STRING.getLangName().equals(type.getName())) {
      throw new IllegalArgumentException("JSON dictionaries can have only string keys. Got key ["
          + key + "] of type " + type);
    }
    getOutput().name(String.valueOf(key));
  }

  @Override
  public void endDictionaryEntry(Type type, Object key) throws IOException {
    // Nothing.
  }

  @Override
  public void beginSequence(Sequence sequence) throws IOException {
    getOutput().beginArray();
  }

  @Override
  public void endSequence(Sequence sequence) throws IOException {
    getOutput().endArray();
  }

  @Override
  public void beginDictionary(Dictionary dictionary) throws IOException {
    getOutput().beginObject();
  }

  @Override
  public void endDictionary(Dictionary dictionary) throws IOException {
    getOutput().endObject();
  }

  @Override
  public void close() throws IOException {
    getOutput().close();
  }
}
