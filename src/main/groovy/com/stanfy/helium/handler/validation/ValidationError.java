package com.stanfy.helium.handler.validation;

import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;

import java.util.List;

/**
 * Validation error description.
 */
public class ValidationError {

  /** Message. */
  private final Message message;

  /** Field. */
  private final Field field;

  /** Explanation. */
  private final String explanation;

  /** Children errors. */
  private List<ValidationError> children;

  public ValidationError(final String explanation) {
    this(null, null, explanation);
  }

  public ValidationError(final Message msg, final String explanation) {
    this(msg, null, explanation);
  }

  public ValidationError(final Message msg, final Field field, final String explanation) {
    this.message = msg;
    this.field = field;
    this.explanation = explanation;
  }

  public Message getMessage() {
    return message;
  }

  public Field getField() {
    return field;
  }

  public String getExplanation() {
    return explanation;
  }

  public List<ValidationError> getChildren() {
    return children;
  }

  public void setChildren(final List<ValidationError> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    StringBuilder res = new StringBuilder().append("[");
    if (message != null) {
      res.append("\nmessage=").append(message);
    }
    if (field != null) {
      if (res.length() > 1) { res.append(","); }
      res.append("\nfield=").append(field);
    }
    if (explanation != null) {
      if (res.length() > 1) { res.append(","); }
      res.append("\nexplanation=").append(explanation);
    }
    if (children != null && !children.isEmpty()) {
      if (res.length() > 1) { res.append(","); }
      res.append("\nchildren=").append(children);
    }
    res.append("\n]");
    return res.toString();
  }

}
