package com.stanfy.helium.internal.entities;

import com.stanfy.helium.format.FormatReader;
import com.stanfy.helium.format.FormatWriter;
import com.stanfy.helium.model.constraints.ConstrainedType;
import com.stanfy.helium.model.constraints.Constraint;

import java.io.IOException;
import java.util.List;

/**
 * Converter for constrained types.
 * It wraps another converter adding constraints check on read.
 */
final class ConstrainedTypeConverter extends BaseConverter<ConstrainedType> {

  /** Converters pool. */
  private final BaseConverter<?> baseConverter;

  public ConstrainedTypeConverter(final ConstrainedType type) {
    super(type);
    this.baseConverter = getConverter(type.getBaseType());
  }

  @Override
  public void writeData(final FormatWriter output, final Object value) throws IOException {
    baseConverter.write(output, value);
  }

  @Override
  public Object readData(final FormatReader input, final List<ValidationError> errors) throws IOException {
    Object result = baseConverter.read(input, errors);
    for (Constraint<Object> c : type.getConstraints()) {
      if (!c.validate(result)) {
        errors.add(new ValidationError(c.describe(result)));
      }
    }
    return result;
  }

}
