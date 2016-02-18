package com.stanfy.helium.format;

import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Type;
import okio.Source;

import java.io.IOException;

/**
 * Interface of a specific format reader.
 */
public interface FormatReader extends Format {

  boolean hasNext() throws IOException;

  boolean checkNextNull() throws IOException;

  void skipValue() throws IOException;

  Object nextValue(Type type) throws IOException, ConvertValueSyntaxException;

  Object nextDictionaryKey(Dictionary type, Type keyType) throws IOException;

  String nextFieldName(Message type) throws IOException;

  /** Interface of a service that can provide plugable source implementation. */
  interface Factory extends FormatProvider<Source, FormatReader> { }

}
