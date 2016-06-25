package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions

/**
 * Created by paultaykalo on 12/17/15.
 * Abstract builder interface that can build objects of type Destination from type Source
 */
interface ObjCBuilder<S, D> {
  /**
   * Performs transformation from class structure to file structure
   */
  fun build(from: S,options: ObjCEntitiesOptions? = null): D

  fun build(from: S) : D {
    return build(from, null)
  }

}

