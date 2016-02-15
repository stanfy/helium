package com.stanfy.helium.handler.codegen.objectivec.entity

import com.stanfy.helium.model.Project

/**
 * Created by ptaykalo on 9/2/14.
 * Interface that generates part of the project structure
 * Updates ObjCProject, by adding additional classes or by updateding classes
 */
public interface ObjCProjectStructureGenerator {

  /**
   * Updated specified |project| by adding additional classes(s), those contain code that
   * should map items from the DSL to the Classes, defined in project.
   */
  // TODO: replace with more generic options
  public fun generate(project: ObjCProjectComplex, projectDSL: Project, options: ObjCEntitiesOptions)
}
