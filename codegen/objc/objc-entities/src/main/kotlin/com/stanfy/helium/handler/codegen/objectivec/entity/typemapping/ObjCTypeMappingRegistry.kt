package com.stanfy.helium.handler.codegen.objectivec.entity.typemapping

import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCType
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type

/**
 * Created by paultaykalo on 2/13/16.
 *
 * Mapping registry, which holds mapping from Helium messages/types to ObjC types
 */

class ObjCTypeMappingRegistry {

  data class ObjCTypeMapping(val heliumType: Type, val objectiveCType: ObjCType)

  /**
   * Registered mappings from helium type names to ObjCType
   */
  private val registeredMappings = hashMapOf<String, ObjCType>()

  fun objcType(heliumType: Type): ObjCType {

    // Check generics
    when (heliumType) {
      is Sequence -> {
        val objCType = ObjCType("NSArray", isReference = true)
        if (heliumType.itemsType != null) {
          objCType.genericOf = objcType(heliumType.itemsType)
        }
        return objCType
      }
    }

    // Check preregistered methods
    when (heliumType.name) {
      "int32"    -> return ObjCType("NSNumber", isReference = true)
      "int64"    -> return ObjCType("NSNumber", isReference = true)
      "long"     -> return ObjCType("NSNumber", isReference = true)
      "bool"     -> return ObjCType("NSNumber", isReference = true)
      "boolean"  -> return ObjCType("NSNumber", isReference = true)
      "float"    -> return ObjCType("NSNumber", isReference = true)
      "float32"  -> return ObjCType("NSNumber", isReference = true)
      "float64"  -> return ObjCType("NSNumber", isReference = true)
      "double"   -> return ObjCType("NSNumber", isReference = true)
      "string"   -> return ObjCType("NSString", isReference = true)
    }


    val registeredType = registeredMappings[heliumType.name]
    if (registeredType != null) {
      return registeredType
    }

    return ObjCType(heliumType.name, isReference = true)
  }

  fun registerMapping(heliumType: Type, type:ObjCType) {
    registeredMappings[heliumType.name] = type
  }


}
