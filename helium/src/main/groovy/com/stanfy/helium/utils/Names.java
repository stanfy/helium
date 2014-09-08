package com.stanfy.helium.utils;

import java.util.regex.Pattern;

/**
 * Names related utils.
 */
public final class Names {

  private static final Pattern REPLACED_WITH_UNDERSCORE = Pattern.compile("[/\\s\\.]+");
  private static final Pattern BAD_CHARACTERS = Pattern.compile("\\W+");

  private Names() {
    throw new UnsupportedOperationException("no instances");
  }

  public static String canonicalName(final String name) {
    if (name == null) {
      throw new IllegalStateException("Name is not defined");
    }

    String result = BAD_CHARACTERS.matcher(
        REPLACED_WITH_UNDERSCORE.matcher(name.trim()).replaceAll("_")
    ).replaceAll("");
    if (result.length() > 1) {
      if (result.charAt(result.length() - 1) == '_') {
        result = result.substring(0, result.length() - 1);
      }
    }
    return result.length() > 0 && result.charAt(0) == '_' ? result.substring(1) : result;
  }

  public static String packageNameToPath(final String name) {
    if (name == null) {
      throw new IllegalArgumentException("package name is not defined");
    }
    return name.replaceAll("\\.", "/");
  }

  public static String prettifiedName(final String s) {
    StringBuilder result = new StringBuilder(s);
    for (int i = 0; i < result.length(); i++) {
      boolean shouldCap = false;
      while (i < result.length() && (result.charAt(i) == '_' || result.charAt(i) == '-')) {
        result.delete(i, i + 1);
        shouldCap = true;
      }
      if (shouldCap && i < result.length()) {
        result.setCharAt(i, Character.toUpperCase(result.charAt(i)));
      }
    }
    return result.toString();
  }

  public static String rootPath(final String path) {
    if (path == null || path.trim().length() == 0) {
      throw new IllegalArgumentException("Path cannot be null or empty");
    }
    return path.charAt(0) == '/' ? path : "/".concat(path);
  }

  public static String capitalize(final String name) {
    if (name == null) {
      return null;
    }

    char[] ch = new char[name.length()];
    name.getChars(0, ch.length, ch, 0);
    ch[0] = Character.toUpperCase(ch[0]);
    return new String(ch);
  }

}
