package com.stanfy.helium.handler.validation.json;

import com.stanfy.helium.handler.validation.ValidationError;
import com.stanfy.helium.model.Type;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Validates whether the incoming JSON conforms the supplied message.
 */
public abstract class JsonValidator {

  /** Message. */
  private final Type type;

  public JsonValidator(final Type type) {
    this.type = type;
  }

  protected static String getFullErrorMessage(final Throwable e) {
    StringWriter stackOut = new StringWriter();
    e.printStackTrace(new PrintWriter(stackOut));
    return stackOut.toString();
  }

  public Type getType() { return type; }

  public List<ValidationError> validate(final String json) {
    return validate(new StringReader(json));
  }

  public abstract List<ValidationError> validate(final Reader reader);

}
