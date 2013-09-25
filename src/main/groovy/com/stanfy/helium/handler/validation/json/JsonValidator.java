package com.stanfy.helium.handler.validation.json;

import com.stanfy.helium.handler.validation.ValidationError;
import com.stanfy.helium.model.Message;

import java.io.*;
import java.util.Collections;
import java.util.List;

/**
 * Validates whether the incoming JSON conforms the supplied message.
 */
public abstract class JsonValidator {

  /** Message. */
  private final Message message;

  public JsonValidator(final Message message) {
    this.message = message;
  }

  protected static String getFullErrorMessage(final Throwable e) {
    StringWriter stackOut = new StringWriter();
    e.printStackTrace(new PrintWriter(stackOut));
    return stackOut.toString();
  }

  public Message getMessage() { return message; }

  public List<ValidationError> validate(final String json) {
    try {
      return validate(new StringReader(json));
    } catch (IOException e) {
      return Collections.singletonList(new ValidationError(message, "Could not parse input JSON\n" + getFullErrorMessage(e)));
    }
  }

  public abstract List<ValidationError> validate(final Reader reader) throws IOException;

}
