package com.stanfy.helium.handler.codegen.objectivec.parser;

import com.stanfy.helium.handler.codegen.objectivec.file.ObjCPropertyDefinition;

/**
 * Created by ptaykalo on 8/25/14.
 * Performs transformation of the Helium API type to the Objective-C Type
 */
public class ObjCTypeTransformer {

  /*
  Returns Objective-C type for specified Helium API Type
   */
  public String objCType(final String heliumAPIType) {
    if (heliumAPIType.equals("string")) {
      return "NSString *";
    }
    return null;
  }

  /*
  Returns access modifier for specified helium type
   */
  public ObjCPropertyDefinition.AccessModifier accessorModifierForType(final String heliumAPIType) {
    if (heliumAPIType.equals("string")) {
      return ObjCPropertyDefinition.AccessModifier.COPY;
    }
    return ObjCPropertyDefinition.AccessModifier.ASSIGN;
  }
}
