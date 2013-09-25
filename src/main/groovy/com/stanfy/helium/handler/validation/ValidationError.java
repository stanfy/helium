package com.stanfy.helium.handler.validation;

import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Type;

import java.util.List;

/**
 * Validation error description.
 */
public class ValidationError {

  /** Message. */
  private final Type type;

  /** Field. */
  private final Field field;

  /** Explanation. */
  private final String explanation;

  /** Children errors. */
  private List<ValidationError> children;

  public ValidationError(final String explanation) {
    this(null, null, explanation);
  }

  public ValidationError(final Type msg, final String explanation) {
    this(msg, null, explanation);
  }

  public ValidationError(final Type msg, final Field field, final String explanation) {
    this.type = msg;
    this.field = field;
    this.explanation = explanation;
  }

  public Type getType() {
    return type;
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
    if (type != null) {
      res.append("\ntype=").append(type);
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
