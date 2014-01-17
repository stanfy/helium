package com.stanfy.helium.entities;

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

  /** Index. */
  private final int index;

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
    this.index = -1;
  }

  public ValidationError(final Type msg, final int index, final String explanation) {
    this.type = msg;
    this.field = null;
    this.explanation = explanation;
    this.index = index;
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

  private static StringBuilder indent(final CharSequence spaces, final StringBuilder res) {
    return res.append('\n').append(spaces);
  }

  private void dump(final int size, final StringBuilder res) {
    StringBuilder spaces = new StringBuilder();
    for (int i = 0; i < size + (index == -1 ? 2 : 4); i++) {
      spaces.append(' ');
    }

    if (field != null) {
      res.append("'").append(field.getName()).append("': ");
    } else if (type != null) {
      res.append(type.getName()).append(": ");
    }
    if (explanation != null) {
      res.append(explanation);
    }

    if (children != null && !children.isEmpty()) {
      for (ValidationError child : children) {
        indent(spaces, res);
        if (child.index == -1) {
          res.append("- ");
          child.dump(size + 2, res);
        } else {
          res.append("[").append(child.index).append("] ");
          child.dump(size + 2, res);
        }
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder res = new StringBuilder();
    dump(0, res);
    return res.toString();
  }

}
