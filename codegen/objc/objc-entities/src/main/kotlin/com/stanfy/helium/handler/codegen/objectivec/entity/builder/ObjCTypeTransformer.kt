package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCType
import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCProperty.*
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type

import java.util.HashMap

/**
 * Created by ptaykalo on 8/25/14.
 * Performs transformation of the Helium API type to the Objective-C Type
 */

public data class ObjCTypeTransformation(val heliumType: String,
                                         val objectiveCType: ObjCType,
                                         val accessModifier: AccessModifier = AccessModifier.ASSIGN)

@Deprecated("Please use ObjcTypeRegistry")
public class ObjCTypeTransformer {

  /**
   * This hashMap holds information about Helium API -> Objective-C type conversions
   */
  private val typesMapping = HashMap<String, ObjCType>()
  private val accessMapping = HashMap<String, AccessModifier>()

  constructor() {
    this.registerTransformation("string", ObjCType("NSString"), AccessModifier.COPY)
    this.registerTransformation("int32", ObjCType("NSNumber"), AccessModifier.STRONG)
    this.registerTransformation("int64", ObjCType("NSNumber"), AccessModifier.STRONG)
    this.registerTransformation("long", ObjCType("NSNumber"), AccessModifier.STRONG)
    this.registerTransformation("int", ObjCType("NSNumber"), AccessModifier.STRONG)
    this.registerTransformation("boolean", ObjCType("NSNumber"), AccessModifier.STRONG)
    this.registerTransformation("bool", ObjCType("NSNumber"), AccessModifier.STRONG)
    this.registerTransformation("float", ObjCType("NSNumber"), AccessModifier.STRONG)
    this.registerTransformation("float32", ObjCType("NSNumber"), AccessModifier.STRONG)
    this.registerTransformation("float64", ObjCType("NSNumber"), AccessModifier.STRONG)
    this.registerTransformation("double", ObjCType("NSNumber"), AccessModifier.STRONG)
  }

  public fun registerTransformations(transformations: List<ObjCTypeTransformation>) {
    transformations.forEach { t -> this.registerTransformation(t) }
  }

  public fun registerTransformation(transformation: ObjCTypeTransformation) {
    typesMapping.put(transformation.heliumType, transformation.objectiveCType)
    accessMapping.put(transformation.heliumType, transformation.accessModifier)
  }
  public fun registerTransformation(heliumTypeName: String, objectiveCType: ObjCType, accessModifier: AccessModifier = AccessModifier.ASSIGN) {
    registerTransformation(ObjCTypeTransformation(heliumTypeName, objectiveCType, accessModifier));
  }

  public fun registerTransformation(heliumTypeName: String, objectiveCType: ObjCType) {
    registerTransformation(ObjCTypeTransformation(heliumTypeName, objectiveCType, AccessModifier.STRONG));
  }

  /**
   * Returns Objective-C type for specified Helium API Type
   */
  public fun objCType(heliumAPIType: Type): ObjCType {
    return this.objCType(heliumAPIType, false)
  }

  public fun objCType(heliumAPIType: Type, isSequence: Boolean): ObjCType {
    if (isSequence) {
      val objCType = ObjCType("NSArray", isReference = true)
      objCType.genericOf = this.objCType(heliumAPIType, isSequence = false)
      return objCType
    }
    if (heliumAPIType is Sequence) {
      val objCType = ObjCType("NSArray", isReference = true)
      if (heliumAPIType.itemsType != null) {
        objCType.genericOf = this.objCType(heliumAPIType.itemsType, isSequence = false)
      }
      return objCType
    }
    val registeredTypeConversion = typesMapping[heliumAPIType.name]
    if (registeredTypeConversion != null) {
      return registeredTypeConversion
    }
    return ObjCType(heliumAPIType.name)
  }

  /**
   * Returns access modifier for specified helium type
   */
  public fun accessorModifierForType(heliumAPIType: Type): AccessModifier {

    if (heliumAPIType is Sequence) {
      return AccessModifier.STRONG
    }

    val accessModifier = accessMapping.get(heliumAPIType.name)
    if (accessModifier != null) {
      return accessModifier
    }

    if (heliumAPIType is Message) {
      return AccessModifier.STRONG
    }

    if ("string".equals(heliumAPIType.name)) {
      return AccessModifier.COPY
    }

    return AccessModifier.ASSIGN
  }
}
