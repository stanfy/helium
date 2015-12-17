package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.file.AccessModifier
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
  private val typesMapping = HashMap<String, String>()
  private val accessMapping = HashMap<String, AccessModifier>()

  constructor() {
    this.registerRefTypeTransformation("string", "NSString", AccessModifier.COPY)
    this.registerSimpleTransformation("int32", "NSInteger")
    this.registerSimpleTransformation("int64", "NSInteger")
    this.registerSimpleTransformation("long", "NSInteger")
    this.registerSimpleTransformation("int", "NSInteger")
    this.registerSimpleTransformation("boolean", "BOOL")
    this.registerSimpleTransformation("bool", "BOOL")
    this.registerSimpleTransformation("float", "double")
    this.registerSimpleTransformation("float32", "double")
    this.registerSimpleTransformation("float64", "double")
    this.registerSimpleTransformation("double", "double")

  }

  public fun registerSimpleTransformation(heliumTypeName: String, objectiveCTypeName: String) {
    typesMapping.put(heliumTypeName, objectiveCTypeName)
    accessMapping.put(heliumTypeName, AccessModifier.ASSIGN)
  }


  public fun registerRefTypeTransformation(heliumTypeName: String, objectiveCTypeName: String) {
    this.registerRefTypeTransformation(heliumTypeName, objectiveCTypeName, AccessModifier.STRONG)
  }

  public fun registerRefTypeTransformation(heliumTypeName: String, objectiveCTypeName: String, accessModifier: AccessModifier) {
    typesMapping.put(heliumTypeName, objectiveCTypeName + " *")
    accessMapping.put(heliumTypeName, accessModifier)
  }

  /**
   * Returns Objective-C type for specified Helium API Type
   */
  public fun objCType(heliumAPIType: Type): String {
    return this.objCType(heliumAPIType, false)
  }

  public fun objCType(heliumAPIType: Type, isSequence: Boolean): String {
    if (isSequence) {
      return "NSArray *"
    }
    if (heliumAPIType is Sequence) {
      return "NSArray *"
    }
    val registeredTypeConversion = typesMapping[heliumAPIType.name]
    if (registeredTypeConversion != null) {
      return registeredTypeConversion
    }
    return heliumAPIType.name
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
