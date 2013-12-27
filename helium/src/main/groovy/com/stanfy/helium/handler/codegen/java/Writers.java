package com.stanfy.helium.handler.codegen.java;

import java.io.Writer;

/**
 * Java writers factories.
 */
public final class Writers {

  public interface WriterFactory {
    MessageToJavaClass createWriter(Writer output, PojoGeneratorOptions options);
  }

  private Writers() {
    throw new UnsupportedOperationException("no instances");
  }

  /**
   * @return POJO writer
   */
  public static WriterFactory pojoWriter() {
    return new WriterFactory() {
      @Override
      public MessageToJavaClass createWriter(final Writer output, final PojoGeneratorOptions options) {
        return new MessageToJavaClass(output, options);
      }
    };
  }


  /**
   * @return writer for object with Google Gson's {@code SerializedName} annotations.
   */
  public static WriterFactory gsonWriter() {
    return new WriterFactory() {
      @Override
      public MessageToJavaClass createWriter(final Writer output, final PojoGeneratorOptions options) {
        return new MessageToGsonClass(output, options);
      }
    };
  }


}
