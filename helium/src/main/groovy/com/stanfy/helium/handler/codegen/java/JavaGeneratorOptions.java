package com.stanfy.helium.handler.codegen.java;

import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Type;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Base class for Java generator options.
 */
public abstract class JavaGeneratorOptions implements Serializable {

  private static final long serialVersionUID = 1;

  /** Package name for generated classes. */
  private String packageName;

  /** Include type name patterns. */
  private Set<String> include = new HashSet<String>();

  /** Exclude type name patterns. */
  private Set<String> exclude = new HashSet<String>();

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(final String packageName) {
    if (packageName == null) {
      throw new IllegalArgumentException("Package name cannot be null");
    }
    this.packageName = packageName;
  }

  public Set<String> getInclude() {
    return include;
  }

  public Set<String> getExclude() {
    return exclude;
  }

  public boolean isTypeUserDefinedMessage(final Type type) {
    return !type.isAnonymous() && type instanceof Message;
  }

  public boolean isTypeIncluded(final Type type) {
    String name = type.getName();
    for (String pattern : exclude) {
      if (Pattern.compile(pattern).matcher(name).matches()) {
        return false;
      }
    }
    if (include.isEmpty()) {
      return true;
    }

    for (String pattern : include) {
      if (Pattern.compile(pattern).matcher(name).matches()) {
        return true;
      }
    }
    return false;
  }

}
