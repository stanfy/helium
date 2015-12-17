package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions

/**
 * Created by paultaykalo on 12/17/15.
 */
interface ObjCBuilder<FROM, TO> {
  /**
   * Perfroms transformation from class structure to file structure
   */
  public fun build(from: FROM): TO

  /**
   *Perfroms transformation from class structure to file structure
   * Uses specified options for the generation @see ObjCProjectParserOptions
   */
  // TODO replace with more generic options
  public fun build(from: FROM, options: ObjCEntitiesOptions?): TO
}