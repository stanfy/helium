package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions

/**
 * Created by paultaykalo on 12/17/15.
 * Abastract builder interface that can build objects of type D(estination) from type (S)ource
 */
interface ObjCBuilder<S, D> {
  /**
   * Performs transformation from class structure to file structure
   */
  public fun build(from: S): D

  /**
   * Performs transformation from class structure to file structure
   * Uses specified options for the generation @see ObjCProjectParserOptions
   */
  // TODO replace with more generic options
  public fun build(from: S, options: ObjCEntitiesOptions?): D
}