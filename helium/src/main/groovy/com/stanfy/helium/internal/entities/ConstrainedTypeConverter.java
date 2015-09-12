package com.stanfy.helium.internal.entities;

import com.stanfy.helium.model.Type;
import com.stanfy.helium.model.constraints.ConstrainedType;
import com.stanfy.helium.model.constraints.Constraint;

import java.io.IOException;
import java.util.List;

/**
 * Converter for constrained types.
 */
final class ConstrainedTypeConverter<I, O> implements Converter<Type, I, O> {

  /** Type instance. */
  private final ConstrainedType type;
  /** Converters pool. */
  private final Converter<Type, I, O> baseConverter;

  public ConstrainedTypeConverter(final ConstrainedType type, final ConvertersPool<I, O> pool) {
    this.type = type;
    this.baseConverter = pool.getConverter(type.getBaseType());
  }

  @Override
  public String getFormat() {
    return baseConverter.getFormat();
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void write(final O output, final Object value) throws IOException {
    baseConverter.write(output, value);
  }

  @Override
  public Object read(final I input, final List<ValidationError> errors) throws IOException {
    Object result = baseConverter.read(input, errors);
    for (Constraint<Object> c : type.getConstraints()) {
      if (!c.validate(result)) {
        errors.add(new ValidationError(c.describe(result)));
      }
    }
    return result;
  }

}
