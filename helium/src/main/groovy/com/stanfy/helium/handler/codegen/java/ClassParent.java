package com.stanfy.helium.handler.codegen.java;

/**
 * Represents class' parent and set of interfaces it's implementing.
 */
public class ClassParent {
  private final String extending;
  private final String[] implementing;

  public ClassParent(final String extending, final String... implementing) {
    this.extending = extending;
    this.implementing = implementing;
  }

  public static ClassParent extending(final String name) {
    return extending(name, (String[]) null);
  }

  public static ClassParent implementing(final String... interfaces) {
    return extending(null, interfaces);
  }

  public static ClassParent extending(final String name, final String... interfaces) {
    return new ClassParent(name, interfaces);
  }

  public String getExtending() {
    return extending;
  }

  public String[] getImplementing() {
    return implementing;
  }
}
