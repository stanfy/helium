package com.stanfy.helium.swagger;

import com.stanfy.helium.model.ServiceMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** Options for SwaggerHandler. */
public class SwaggerOptions {

  /** What endpoints into Swagger spec. */
  private List<Pattern> includes = Collections.emptyList();

  private static void checkNotNull(Object arg) {
    if (arg == null) {
      throw new IllegalArgumentException("Argument cannot be null");
    }
  }

  public void includePatterns(List<Pattern> includes) {
    checkNotNull(includes);
    this.includes = includes;
  }

  public void includes(List<String> includes) {
    checkNotNull(includes);
    this.includes = new ArrayList<>(includes.size());
    for (String arg : includes) {
      this.includes.add(Pattern.compile(arg));
    }
  }

  public void includes(String... includes) {
    includes(Arrays.asList(includes));
  }

  public List<Pattern> getIncludes() {
    return includes;
  }

  boolean checkIncludes(ServiceMethod m) {
    if (includes.isEmpty()) {
      return true;
    }

    String name = m.getType().name() + " " + m.getPath();
    for (Pattern p : includes) {
      if (p.matcher(name).matches()) {
        return true;
      }
    }
    return false;
  }

}
