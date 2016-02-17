package com.stanfy.helium.internal.entities;

import com.stanfy.helium.format.Format;
import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;

import java.io.IOException;

class DelegateFormat<T extends Format> implements Format {

  final T delegate;

  protected DelegateFormat(T delegate) {
    this.delegate = delegate;
  }

  @Override
  public void beginDictionary(Dictionary dictionary) throws IOException {
    delegate.beginDictionary(dictionary);
  }

  @Override
  public void endDictionary(Dictionary dictionary) throws IOException {
    delegate.endDictionary(dictionary);
  }

  @Override
  public void beginMessage(Message message) throws IOException {
    delegate.beginMessage(message);
  }

  @Override
  public void endMessage(Message message) throws IOException {
    delegate.endMessage(message);
  }

  @Override
  public void beginSequence(Sequence sequence) throws IOException {
    delegate.beginSequence(sequence);
  }

  @Override
  public void endSequence(Sequence sequence) throws IOException {
    delegate.endSequence(sequence);
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

}
