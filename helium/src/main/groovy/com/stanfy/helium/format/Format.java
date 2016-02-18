package com.stanfy.helium.format;

import com.squareup.okhttp.MediaType;
import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

public interface Format extends Closeable {

  void beginMessage(Message message) throws IOException;

  void endMessage(Message message) throws IOException;

  void beginSequence(Sequence sequence) throws IOException;

  void endSequence(Sequence sequence) throws IOException;

  void beginDictionary(Dictionary dictionary) throws IOException;

  void endDictionary(Dictionary dictionary) throws IOException;

  /** Base factory interface for FormatSink and FormatSource. */
  interface FormatProvider<Target, FR extends Format> {

    boolean supportsMediaType(MediaType type);

    FR create(Target target, Charset charset);

  }
}
