package com.stanfy.helium.format.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.stanfy.helium.DefaultType;
import com.stanfy.helium.format.BaseFormatReader;
import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.IOException;

final class JsonReadFormat extends BaseFormatReader<JsonReader> {

  JsonReadFormat(JsonReader input) {
    super(input);
  }

  @Override
  public boolean hasNext() throws IOException {
    return getInput().hasNext();
  }

  @Override
  public boolean checkNextNull() throws IOException {
    return getInput().peek() == JsonToken.NULL;
  }

  @Override
  public void skipValue() throws IOException {
    getInput().skipValue();
  }

  @Override
  public Object nextDictionaryKey(Dictionary type, Type keyType) throws IOException {
    if (!DefaultType.STRING.getLangName().equals(keyType.getName())) {
      throw new IllegalStateException("JSON dictionary keys can have string type only. Got "
          + keyType + " in " + type);
    }
    return getInput().nextName();
  }

  @Override
  public String nextFieldName(Message type) throws IOException {
    return getInput().nextName();
  }

  @Override
  public void beginMessage(Message message) throws IOException {
    getInput().beginObject();
  }

  @Override
  public void endMessage(Message message) throws IOException {
    getInput().endObject();
  }

  @Override
  public void beginSequence(Sequence sequence) throws IOException {
    getInput().beginArray();
  }

  @Override
  public void endSequence(Sequence sequence) throws IOException {
    getInput().endArray();
  }

  @Override
  public void beginDictionary(Dictionary dictionary) throws IOException {
    getInput().beginObject();
  }

  @Override
  public void endDictionary(Dictionary dictionary) throws IOException {
    getInput().endObject();
  }

  @Override
  public void close() throws IOException {
    getInput().close();
  }
}
