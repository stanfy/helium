package com.stanfy.helium.handler.codegen.objectivec.mapper;

import com.stanfy.helium.handler.codegen.objectivec.ObjCProject;
import com.stanfy.helium.handler.codegen.objectivec.parser.options.ObjCProjectParserOptions;
import com.stanfy.helium.model.Project;

/**
 * Created by ptaykalo on 9/2/14.
 * Interface that performs mappings generation for specified project/SDL
 * Updates ObjCProject, by adding additional files, those contains mappers
 */
public interface ObjCMapper {

  /*
  Updated specified |project| by adding additional file(s), those contain code that
  should map items from the DSL to the Classes, defined in project.
   */
  void generateMappings(final ObjCProject project, final Project projectDSL, final ObjCProjectParserOptions options);
}
