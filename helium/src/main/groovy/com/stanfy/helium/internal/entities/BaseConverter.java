package com.stanfy.helium.internal.entities;

import com.stanfy.helium.format.ConvertValueSyntaxException;
import com.stanfy.helium.format.FormatReader;
import com.stanfy.helium.format.FormatWriter;
import com.stanfy.helium.model.Dictionary;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.constraints.ConstrainedType;

import java.io.IOException;
import java.util.List;

abstract class BaseConverter<T extends Type> {

  final T type;

  protected BaseConverter(T type) {
    this.type = type;
  }

  public final T getType() {
    return type;
  }

  public final Object read(FormatReader input, List<ValidationError> errors) throws IOException {
    try {
      return readData(wrapReader(input), errors);
    } catch (SafeFormat.StructureProblem e) {
      errors.add(new ValidationError(type, e.getMessage() + " Reason: " + e.getCause().getMessage()));
      input.skipValue();
      return null;
    }
  }

  private static FormatReader wrapReader(FormatReader input) {
    return input instanceof SafeFormat.SafeReader ? input : new SafeFormat.SafeReader(input);
  }

  protected abstract Object readData(FormatReader input, List<ValidationError> errors) throws IOException;

  public final void write(FormatWriter output, Object value) throws IOException {
    writeData(wrapWriter(output), value);
  }

  private static FormatWriter wrapWriter(FormatWriter output) {
    return output instanceof SafeFormat.SafeWriter ? output : new SafeFormat.SafeWriter(output);
  }

  public abstract void writeData(FormatWriter output, Object value) throws IOException;

  public static BaseConverter<? extends Type> getConverter(Type type) {
    if (type instanceof Message) {
      return new MessageConverter((Message) type);
    } else if (type instanceof Sequence) {
      return new SequenceConverter((Sequence) type);
    } else if (type instanceof Dictionary) {
      return new DictionaryConverter((Dictionary) type);
    } else if (type instanceof ConstrainedType) {
      return new ConstrainedTypeConverter((ConstrainedType) type);
    } else if (type.isPrimitive()) {
      return new BaseConverter<Type>(type) {
        @Override
        public Object readData(FormatReader input, List<ValidationError> errors) throws IOException {
          try {
            if (input.checkNextNull()) {
              return null;
            }
            return input.nextValue(this.type);
          } catch (IllegalArgumentException | IllegalStateException | ConvertValueSyntaxException e) {
            errors.add(new ValidationError(e.getMessage()));
            return null;
          }
        }

        @Override
        public void writeData(FormatWriter output, Object value) throws IOException {
          try {
            output.value(this.type, value);
          } catch (ConvertValueSyntaxException e) {
            throw new IllegalStateException(e.getMessage(), e);
          }
        }
      };
    }
    throw new UnsupportedOperationException("Cannot convert " + type);
  }

}
