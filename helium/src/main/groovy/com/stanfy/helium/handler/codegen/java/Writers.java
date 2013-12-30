package com.stanfy.helium.handler.codegen.java;

import java.io.Writer;

/**
 * Java writers factories.
 */
public final class Writers {

  public interface WriterFactory {
    JavaClassWriter createWriter(Writer output);
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
      public JavaClassWriter createWriter(final Writer output) {
        return new PojoWriter(output);
      }
    };
  }


  /**
   * @return writer for object with Google Gson's {@code SerializedName} annotations.
   */
  public static WriterFactory gsonWriter() {
    return new WriterFactory() {
      @Override
      public JavaClassWriter createWriter(final Writer output) {
        return new GsonPojoWriter(pojoWriter().createWriter(output));
      }
    };
  }


}
