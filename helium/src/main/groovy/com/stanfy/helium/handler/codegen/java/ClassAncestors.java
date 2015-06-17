package com.stanfy.helium.handler.codegen.java;

/**
 * Represents class' parent and set of interfaces it's implementing.
 */
public class ClassAncestors {
  private final String extending;
  private final String[] implementing;

  public ClassAncestors(final String extending, final String... implementing) {
    this.extending = extending;
    this.implementing = implementing;
  }

  public static ClassAncestors extending(final String name) {
    return extending(name, new String[]{});
  }

  public static ClassAncestors implementing(final String... interfaces) {
    return extending(null, interfaces);
  }

  public static ClassAncestors extending(final String name, final String... interfaces) {
    return new ClassAncestors(name, interfaces);
  }

  public String getExtending() {
    return extending;
  }

  public String[] getImplementing() {
    return implementing;
  }
}
