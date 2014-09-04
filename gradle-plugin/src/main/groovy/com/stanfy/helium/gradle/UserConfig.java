package com.stanfy.helium.gradle;

import com.stanfy.helium.utils.Names;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Project;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Internal structure that stores user configuration.
 */
class UserConfig {

  /** Gradle project. */
  final Project project;

  /** Default source generation data. */
  SourceGenDslDelegate defaultSourceGeneration;

  /** Source generation rules per each spec. */
  private final Map<File, SourceGenDslDelegate> specSourceGeneration = new HashMap<File, SourceGenDslDelegate>();

  /** Variables binding passed to all the tasks. */
  final Map<String, String> variables = new LinkedHashMap<String, String>();

  UserConfig(final Project project) {
    this.project = project;
  }

  public static String specName(final File specFile) {
    String name = FilenameUtils.getBaseName(specFile.getName());
    return Names.prettifiedName(name);
  }

  public void set(final File spec, final SourceGenDslDelegate delegate) {
    specSourceGeneration.put(spec, delegate);
  }

  public SourceGenDslDelegate getSourceGenFor(final File spec) {
    SourceGenDslDelegate res = specSourceGeneration.get(spec);
    if (res == null) {
      return defaultSourceGeneration;
    }
    if (defaultSourceGeneration != null) {
      for (String gen : defaultSourceGeneration.allGenerators()) {
        if (res.getDelegate(gen) == null) {
          res.setDelegate(gen, defaultSourceGeneration.getDelegate(gen));
        }
      }
    }
    return res;
  }

}
