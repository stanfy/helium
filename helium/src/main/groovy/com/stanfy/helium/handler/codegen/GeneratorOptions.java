package com.stanfy.helium.handler.codegen;

import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Type;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Base class for generators options.
 */
public abstract class GeneratorOptions implements Serializable {
  private static final long serialVersionUID = 1;

  /** Include type name patterns. */
  private Set<String> include = new HashSet<String>();

  /** Exclude type name patterns. */
  private Set<String> exclude = new HashSet<String>();

  public Set<String> getInclude() {
    return include;
  }

  public Set<String> getExclude() {
    return exclude;
  }

  public boolean isTypeIncluded(final Type type) {
    String name = type.getName();
    for (String pattern : getExclude()) {
      if (Pattern.compile(pattern).matcher(name).matches()) {
        return false;
      }
    }
    if (getInclude().isEmpty()) {
      return true;
    }

    for (String pattern : getInclude()) {
      if (Pattern.compile(pattern).matcher(name).matches()) {
        return true;
      }
    }
    return false;
  }

  public boolean isTypeUserDefinedMessage(final Type type) {
    return !type.isAnonymous() && type instanceof Message;
  }
}
