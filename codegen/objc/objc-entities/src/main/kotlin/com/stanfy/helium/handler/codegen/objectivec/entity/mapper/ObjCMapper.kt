package com.stanfy.helium.handler.codegen.objectivec.entity.mapper;

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions;
import com.stanfy.helium.model.Project;

/**
 * Created by ptaykalo on 9/2/14.
 * Interface that performs mappings generation for specified project/SDL
 * Updates ObjCProject, by adding additional files, those contains mappers
 */
public interface ObjCMapper {

  /**
   * Updated specified |project| by adding additional file(s), those contain code that
   * should map items from the DSL to the Classes, defined in project.
   */
  // TODO: replace with more generic options
  public fun generateMappings(project: ObjCProject, projectDSL: Project, options: ObjCEntitiesOptions)
}
