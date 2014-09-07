package com.stanfy.helium.handler.codegen.objectivec.builder;

import com.stanfy.helium.handler.codegen.objectivec.ObjCProject;
import com.stanfy.helium.handler.codegen.objectivec.ObjcEntitiesOptions;
import com.stanfy.helium.model.Project;

/**
 * Created by ptaykalo on 9/2/14.
 * Interface for the class that can generate valid ObjCProject structure, based on provided
 * Helium DSL Project description
 */
public interface ObjCProjectBuilder {
  /**
   * Returns type transformer, which is responsible for convertation.
   * Of types, described in Porject DSL to ObjC types.
   */
  ObjCTypeTransformer getTypeTransformer();

  ObjCPropertyNameTransformer getNameTransformer();

  /*
    Performs parsing / translation of Helium DSL Proejct Structure to Objective-C Project structure
     */
  ObjCProject build(final Project project);

  /*
    Performs parsing / translation of Helium DSL Proejct Structure to Objective-C Project structure
    Uses specified options for the generation @see ObjCProjectParserOptions
     */
  // TODO replace with more generic options
  ObjCProject build(final Project project, final ObjcEntitiesOptions options);
}
