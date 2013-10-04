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

  private static StringBuilder indent(final CharSequence spaces, final StringBuilder res) {
    return res.append('\n').append(spaces);
  }

  private void dump(final int size, final StringBuilder res) {
    StringBuilder indent = new StringBuilder();
    for (int i = 0; i < size; i++) { indent.append(' '); }

    indent(indent, res).append("[");
    if (type != null) {
      indent(indent, res).append("type=").append(type);
    }
    if (field != null) {
      if (res.length() > 1) { res.append(","); }
      indent(indent, res).append("field=").append(field);
    }
    if (explanation != null) {
      if (res.length() > 1) { res.append(","); }
      indent(indent, res).append("explanation=").append(explanation);
    }
    if (children != null && !children.isEmpty()) {
      if (res.length() > 1) { res.append(","); }
      for (ValidationError child : children) {
        child.dump(size + 2, res);
      }
    }
    indent(indent, res).append("]");
  }

  @Override
  public String toString() {
    StringBuilder res = new StringBuilder();
    dump(0, res);
    return res.toString();
  }

}
