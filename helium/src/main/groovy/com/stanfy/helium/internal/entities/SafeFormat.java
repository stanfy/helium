package com.stanfy.helium.internal.entities;

import com.stanfy.helium.format.ConvertValueSyntaxException;
import com.stanfy.helium.format.Format;
import com.stanfy.helium.format.FormatReader;
import com.stanfy.helium.format.FormatWriter;
import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

/** Wraps format method invocations with additional errors handling. */
class SafeFormat<T extends Format> extends DelegateFormat<T> {

  protected SafeFormat(T delegate) {
    super(delegate);
  }

  @Override
  public void beginDictionary(Dictionary dictionary) throws IOException {
    try {
      super.beginDictionary(dictionary);
    } catch (IllegalStateException e) {
      throw new StructureProblem(dictionary, e, true);
    }
  }

  @Override
  public void endDictionary(Dictionary dictionary) throws IOException {
    try {
      super.endDictionary(dictionary);
    } catch (IllegalStateException e) {
      throw new StructureProblem(dictionary, e, false);
    }
  }

  @Override
  public void beginMessage(Message message) throws IOException {
    try {
      super.beginMessage(message);
    } catch (IllegalStateException e) {
      throw new StructureProblem(message, e, true);
    }
  }

  @Override
  public void endMessage(Message message) throws IOException {
    try {
      super.endMessage(message);
    } catch (IllegalStateException e) {
      throw new StructureProblem(message, e, false);
    }
  }

  @Override
  public void beginSequence(Sequence sequence) throws IOException {
    try {
      super.beginSequence(sequence);
    } catch (IllegalStateException e) {
      throw new StructureProblem(sequence, e, true);
    }
  }

  @Override
  public void endSequence(Sequence sequence) throws IOException {
    try {
      super.endSequence(sequence);
    } catch (IllegalStateException e) {
      throw new StructureProblem(sequence, e, false);
    }
  }

  /** Safe implementation of FormatReader. */
  static class SafeReader extends SafeFormat<FormatReader> implements FormatReader {

    protected SafeReader(FormatReader delegate) {
      super(delegate);
    }

    @Override
    public boolean hasNext() throws IOException {
      return delegate.hasNext();
    }

    @Override
    public boolean checkNextNull() throws IOException {
      return delegate.checkNextNull();
    }

    @Override
    public void skipValue() throws IOException {
      delegate.skipValue();
    }

    @Override
    public Object nextValue(Type type) throws IOException, ConvertValueSyntaxException {
      try {
        return delegate.nextValue(type);
      } catch (Exception e) {
        if (e instanceof IOException
            | e instanceof ConvertValueSyntaxException
            | e instanceof UnsupportedOperationException) {
          throw e;
        }
        Throwable cause = e;
        if (e instanceof UndeclaredThrowableException) {
          cause = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
        }
        throw new IllegalStateException("Bad value of type " + type + ". "
            + cause.getMessage(), cause);
      }
    }

    @Override
    public Object nextDictionaryKey(Dictionary type, Type keyType) throws IOException {
      return delegate.nextDictionaryKey(type, keyType);
    }

    @Override
    public String nextFieldName(Message type) throws IOException {
      return delegate.nextFieldName(type);
    }
  }

  static class SafeWriter extends SafeFormat<FormatWriter> implements FormatWriter {

    protected SafeWriter(FormatWriter delegate) {
      super(delegate);
    }

    @Override
    public void beginMessageField(Field field) throws IOException {
      delegate.beginMessageField(field);
    }

    @Override
    public void endMessageField(Field field) throws IOException {
      delegate.endMessageField(field);
    }

    @Override
    public void beginDictionaryEntry(Type type, Object key) throws IOException {
      delegate.beginDictionaryEntry(type, key);
    }

    @Override
    public void endDictionaryEntry(Type type, Object key) throws IOException {
      delegate.endDictionaryEntry(type, key);
    }

    @Override
    public void value(Type type, Object value) throws IOException, ConvertValueSyntaxException {
      delegate.value(type, value);
    }
  }

  /** Exception thrown when a structure problem is identified. */
  static final class StructureProblem extends IOException {
    private StructureProblem(Type expected, IllegalStateException cause, boolean begin) {
      super("Expected " + (begin ? "begin" : "end") + " of " + expected + " but got some different structure", cause);
    }
  }

}
