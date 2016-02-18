package com.stanfy.helium;

import com.stanfy.helium.internal.dsl.ProjectDsl;
import com.stanfy.helium.model.Project;

/**
 * Default types loader.
 */
final class DefaultTypesLoader {

  private DefaultTypesLoader() { }

  public static void loadFor(Project project) {
    ProjectDsl p = (ProjectDsl) project;
    for (DefaultType t : DefaultType.values()) {
      p.type(t.getLangName());
      p.applyPendingTypes();
      t.setType(p.getTypes().byName(t.getLangName()));
    }
  }

}
