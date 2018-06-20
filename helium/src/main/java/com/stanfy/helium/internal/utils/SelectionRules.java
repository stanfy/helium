package com.stanfy.helium.internal.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Include/exclude rules. */
public class SelectionRules {

  List<Pattern> includes = Collections.emptyList(), excludes = Collections.emptyList();

  private final String name;

  private final Map<String, SelectionRules> nested = new HashMap<>();

  public SelectionRules(String name) {
    this.name = name;
  }

  private static void checkNotNull(Object arg) {
    if (arg == null) {
      throw new IllegalArgumentException("Argument cannot be null");
    }
  }

  public void nest(SelectionRules rules) {
    nested.put(rules.name, rules);
  }

  public SelectionRules nested(String name) {
    return nested.get(name);
  }

  public void includePatterns(List<Pattern> includes) {
    checkNotNull(includes);
    this.includes = includes;
  }

  public void excludePatterns(List<Pattern> excludes) {
    checkNotNull(excludes);
    this.excludes = excludes;
  }

  private static List<Pattern> convert(List<String> input) {
    List<Pattern> result = new ArrayList<>(input.size());
    for (String arg : input) {
      result.add(Pattern.compile(arg));
    }
    return result;
  }

  public void includes(List<String> includes) {
    checkNotNull(includes);
    includePatterns(convert(includes));
  }

  public void excludes(List<String> excludes) {
    checkNotNull(excludes);
    excludePatterns(convert(excludes));
  }

  public void includes(String... includes) {
    includes(Arrays.asList(includes));
  }

  public void excludes(String... excludes) {
    excludes(Arrays.asList(excludes));
  }

  public boolean check(String input) {
    boolean included = false;

    if (includes.isEmpty()) {
      included = true;
    } else {
      for (Pattern p : includes) {
        if (p.matcher(input).matches()) {
          included = true;
          break;
        }
      }
    }

    if (excludes.isEmpty()) {
      return included;
    }
    if (included) {
      for (Pattern p : excludes) {
        if (p.matcher(input).matches()) {
          return false;
        }
      }
    }
    return included;
  }
  
}
