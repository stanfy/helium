package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCType
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.AccessModifier
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Sequence
import com.stanfy.helium.model.Type

import java.util.HashMap

/**
 * Created by ptaykalo on 8/25/14.
 * Performs transformation of the Helium API type to the Objective-C Type
 */
public class ObjCTypeTransformer {

  /**
   * This hashMap holds information about Helium API -> Objective-C type conversions
   */
  private val typesMapping = HashMap<String, ObjCType>()
  private val accessMapping = HashMap<String, AccessModifier>()

  constructor() {
    this.registerTransformation("string", ObjCType("NSString"), AccessModifier.COPY)
    this.registerTransformation("int32", ObjCType("NSInteger", isReference = false), AccessModifier.ASSIGN)
    this.registerTransformation("int64", ObjCType("NSInteger", isReference = false), AccessModifier.ASSIGN)
    this.registerTransformation("long", ObjCType("NSInteger", isReference = false), AccessModifier.ASSIGN)
    this.registerTransformation("int", ObjCType("NSInteger", isReference = false), AccessModifier.ASSIGN)
    this.registerTransformation("boolean", ObjCType("BOOL", isReference = false), AccessModifier.ASSIGN)
    this.registerTransformation("bool", ObjCType("BOOL", isReference = false), AccessModifier.ASSIGN)
    this.registerTransformation("float", ObjCType("double", isReference = false), AccessModifier.ASSIGN)
    this.registerTransformation("float32", ObjCType("double", isReference = false), AccessModifier.ASSIGN)
    this.registerTransformation("float64", ObjCType("double", isReference = false), AccessModifier.ASSIGN)
    this.registerTransformation("double", ObjCType("double", isReference = false), AccessModifier.ASSIGN)
  }

  public fun registerTransformation(heliumTypeName: String, objectiveCType: ObjCType, accessModifier: AccessModifier = AccessModifier.ASSIGN) {
    typesMapping.put(heliumTypeName, objectiveCType)
    accessMapping.put(heliumTypeName, accessModifier)
  }

  public fun registerTransformation(heliumTypeName: String, objectiveCType: ObjCType) {
    typesMapping.put(heliumTypeName, objectiveCType)
    accessMapping.put(heliumTypeName, AccessModifier.STRONG)
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
