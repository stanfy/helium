package com.stanfy.helium.internal.entities;

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

  public ValidationError(final Type type, final String explanation) {
    this(type, null, explanation);
  }

  public ValidationError(final Type type, final Field field, final String explanation) {
    this(type, field, -1, explanation);
  }

  public ValidationError(final Type type, final int index, final String explanation) {
    this(type, null, index, explanation);
  }

  public ValidationError(final Type msg, final Field field, final int index, final String explanation) {
    this.type = msg;
    this.field = field;
    this.explanation = explanation;
    this.index = index;
  }

  public static ValidationError wrap(final Type type, final List<ValidationError> errors, final boolean canMerge) {
    if (errors.isEmpty()) {
      return null;
    }
    if (canMerge && errors.size() == 1) {
      return errors.get(0);
    }
    ValidationError error = new ValidationError(type, "entity got errors");
    error.setChildren(errors);
    return error;
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

  public int getIndex() {
    return index;
  }

  public void setChildren(final List<ValidationError> children) {
    this.children = children;
  }

  private static StringBuilder indent(final CharSequence spaces, final StringBuilder res) {
    return res.append('\n').append(spaces);
  }

  private void dump(final int size, final StringBuilder res) {
    StringBuilder spaces = new StringBuilder();
    for (int i = 0; i < size; i++) {
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
          child.dump(size + 4, res);
        }
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder res = new StringBuilder();
    dump(2, res);
    return res.toString();
  }

}
