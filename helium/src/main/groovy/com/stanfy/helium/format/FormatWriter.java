package com.stanfy.helium.format;

import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Type;
import okio.Sink;

import java.io.IOException;

/** Basic writer interface that can be implemented to serialize entities to a specific format. */
public interface FormatWriter extends Format {

  void beginMessageField(Field field) throws IOException;

  void endMessageField(Field field) throws IOException;

  void beginDictionaryEntry(Type type, Object key) throws IOException;

  void endDictionaryEntry(Type type, Object key) throws IOException;

  void value(Type type, Object value) throws IOException, ConvertValueSyntaxException;

  /** Interface of a service that can provide plugable sink implementation. */
  interface Factory extends FormatProvider<Sink, FormatWriter> { }

}
