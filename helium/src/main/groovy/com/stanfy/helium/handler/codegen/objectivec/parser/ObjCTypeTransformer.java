package com.stanfy.helium.handler.codegen.objectivec.parser;

import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Sequence;
import com.stanfy.helium.model.Type;
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCPropertyDefinition.AccessModifier;

import java.util.HashMap;

//import static com.stanfy.helium.handler.codegen.objectivec.file.ObjCPropertyDefinition.*;

/**
 * Created by ptaykalo on 8/25/14.
 * Performs transformation of the Helium API type to the Objective-C Type
 */
public class ObjCTypeTransformer {

  /*
  This hashmap holds information about Helium API -> Objective-C type conversions
   */
  private HashMap<String, String> typesMapping =  new HashMap<String, String>();
  private HashMap<String, AccessModifier> accessMapping =  new HashMap<String, AccessModifier>();

  public ObjCTypeTransformer() {
    this.registerRefTypeTransformation("string", "NSString", AccessModifier.COPY);
    this.registerSimpleTransformation("int32", "NSInteger");
    this.registerSimpleTransformation("int64", "NSInteger");
    this.registerSimpleTransformation("long", "NSInteger");
    this.registerSimpleTransformation("int", "NSInteger");
    this.registerSimpleTransformation("boolean", "BOOL");
    this.registerSimpleTransformation("bool", "BOOL");
    this.registerSimpleTransformation("float", "double");
    this.registerSimpleTransformation("float32", "double");
    this.registerSimpleTransformation("float64", "double");
    this.registerSimpleTransformation("double", "double");

  }

  public void registerSimpleTransformation(final String heliumTypeName, final String objectiveCTypeName) {
    typesMapping.put(heliumTypeName, objectiveCTypeName);
    accessMapping.put(heliumTypeName, AccessModifier.ASSIGN);
  }


  public void registerRefTypeTransformation(final String heliumTypeName, final String objectiveCTypeName) {
    this.registerRefTypeTransformation(heliumTypeName, objectiveCTypeName, AccessModifier.STRONG);
  }

  public void registerRefTypeTransformation(final String heliumTypeName, final String objectiveCTypeName, final AccessModifier accessModifier) {
    typesMapping.put(heliumTypeName, objectiveCTypeName + " *");
    accessMapping.put(heliumTypeName, accessModifier);
  }

  /*
    Returns Objective-C type for specified Helium API Type
     */
  public String objCType(final Type heliumAPIType) {
    return this.objCType(heliumAPIType, false);
  }

  public String objCType(final Type heliumAPIType, final boolean isSequence) {
    if (isSequence) {
      return "NSArray *";
    }
    if (heliumAPIType instanceof Sequence) {
      return "NSArray *";
    }
    String registeredTypeConversion = typesMapping.get(heliumAPIType.getName());
    if (registeredTypeConversion != null) {
      return registeredTypeConversion;
    }
    return heliumAPIType.getName();
  }

  /*
  Returns access modifier for specified helium type
   */
  public AccessModifier accessorModifierForType(final Type heliumAPIType) {

    if (heliumAPIType instanceof Sequence) {
      return AccessModifier.STRONG;
    }

    AccessModifier accessModifier = accessMapping.get(heliumAPIType.getName());
    if (accessModifier != null) {
      return accessModifier;
    }

    if (heliumAPIType instanceof Message) {
      return AccessModifier.STRONG;
    }

    if ("string".equals(heliumAPIType.getName())) {
      return AccessModifier.COPY;
    }
    return AccessModifier.ASSIGN;
  }
}
