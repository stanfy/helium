package com.stanfy.helium.handler.codegen.java.entity;

import java.io.Serializable;
import java.io.Writer;

/**
 * Java writers factories.
 */
public final class Writers {

  private Writers() {
    throw new UnsupportedOperationException("no instances");
  }

  /**
   * @return POJO writer factory
   */
  public static WriterFactory pojo() {
    return new WriterFactory() {
      @Override
      public JavaClassWriter create(final Writer output) {
        return new PojoWriter(output);
      }
    };
  }


  /**
   * @return writer for object with Google Gson's {@code SerializedName} annotations.
   */
  public static WriterWrapper gson() {
    return new WriterWrapper() {
      @Override
      public JavaClassWriter wrapWriter(final JavaClassWriter output, final EntitiesGeneratorOptions options) {
        return new GsonPojoWriter(output);
      }
    };
  }

  /**
   * @return writer for object that implements android.os.Parcelable
   */
  public static WriterWrapper androidParcelable() {
    return new WriterWrapper() {
      @Override
      public JavaClassWriter wrapWriter(final JavaClassWriter output, final EntitiesGeneratorOptions options) {
        return new AndroidParcelableWriter(output, options);
      }
    };
  }

  /**
   * @return writer for object with Jackson {@code JsonProperty} annotations.
   */
  public static WriterWrapper jackson() {
    return new WriterWrapper() {
      @Override
      public JavaClassWriter wrapWriter(final JavaClassWriter output, final EntitiesGeneratorOptions options) {
        return new JacksonPojoWriter(output);
      }
    };
  }

  /**
   * @param wrappers wrappers sequence
   * @return chained wrapper
   */
  public static WriterWrapper chain(final WriterWrapper... wrappers) {
    return new WriterWrapper() {
      @Override
      public JavaClassWriter wrapWriter(final JavaClassWriter delegate, final EntitiesGeneratorOptions options) {
        JavaClassWriter result = delegate;
        for (WriterWrapper wrapper : wrappers) {
          result = wrapper.wrapWriter(result, options);
        }
        return result;
      }
    };
  }

  public interface WriterWrapper extends Serializable {
    JavaClassWriter wrapWriter(JavaClassWriter delegate, EntitiesGeneratorOptions options);
  }

  public interface WriterFactory extends Serializable {
    JavaClassWriter create(Writer output);
  }

}
