package com.stanfy.helium.handler.codegen.objectivec.entity.builder;

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions;
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
  public fun getTypeTransformer():ObjCTypeTransformer

  /**
   * Returns transformer, that is responsible for generation valid names for properties
   */
  public fun getNameTransformer():ObjCPropertyNameTransformer

  /**
   * Performs parsing / translation of Helium DSL Project Structure to Objective-C Project structure
   */
  public fun build(project:Project):ObjCProject

  /**
   * Performs parsing / translation of Helium DSL Project Structure to Objective-C Project structure
   * Uses specified options for the generation @see ObjCProjectParserOptions
   */
  // TODO replace with more generic options
  public fun build(project:Project, options:ObjCEntitiesOptions?):ObjCProject
}
