package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.typemapping.ObjCTypeMappingRegistry

/**
 * Created by paultaykalo on 2/15/16.
 */
class ObjCProjectBuildContext(val typeMappingRegistry: ObjCTypeMappingRegistry,
                                     val nameTransformer: ObjCPropertyNameTransformer,
                                     val options:ObjCEntitiesOptions?) {

}