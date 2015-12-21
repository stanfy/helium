package com.stanfy.helium.handler.codegen.objectivec.entity.builder;

import com.stanfy.helium.internal.utils.Names
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by ptaykalo on 8/25/14.
 * Transform property names for specified class
 * Updates original property names to another, name those aren't keywords in Objective-C
 */
public class ObjCPropertyNameTransformer {

  companion object {
    val KEYWORDS = HashSet<String>(Arrays.asList("auto", "break", "case", "char", "const", "continue", "default", "do", "double",
        "else", "enum", "extern", "float", "for", "goto", "if", "inline", "int", "long", "register", "restrict", "return",
        "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while",
        "_Bool", "_Complex", "_Imaginery", "BOOL", "Class", "bycopy", "byref", "id", "IMP", "in", "inout", "nil", "NO", "NULL",
        "oneway", "out", "Protocol", "SEL", "self", "super", "YES", "interface", "end", "implementation", "protocol", "class",
        "public", "protected", "private", "property", "try", "throw", "catch", "finally", "synthesize", "dynamic",
        "selector", "atomic", "nonatomic", "retain", "copy", "assign"));

  }

  public fun propertyNameFrom(propertyName: String): String {
    return propertyNameFrom(propertyName, null);
  }

  public fun propertyNameFrom(propertyName: String, nonAllowedNames: Set<String>?): String {
    // update Property name :)
    var currPropertyName = propertyName
    if (currPropertyName == "id") {
      currPropertyName = "ID"
    }
    if (currPropertyName == "description") {
      currPropertyName = "descr"
    }

    if (!isNameAllowed(currPropertyName, nonAllowedNames)) {
      currPropertyName += "Field"
    }
    // First generated property is ok
    // Check if the generated name is in restricted set or keyworkds
    val generatedName = toCamelCase(currPropertyName)
    var resultName = generatedName
    var i = 0;

    if (resultName == "id") {
      resultName = "ID"
    }
    if (resultName == "description") {
      resultName = "descr"
    }

    while (!isNameAllowed(resultName, nonAllowedNames)) {
      resultName = generatedName + i;
      i++;
    }
    return resultName;
  }

  private fun isNameAllowed(propertyName: String, nonAllowedNames: Set<String>?): Boolean {
    if (KEYWORDS.contains(propertyName)) {
      return false;
    }
    if (nonAllowedNames != null && nonAllowedNames.contains(propertyName)) {
      return false;
    }
    return true;
  }


  private fun toCamelCase(value: String): String {
    return Names.prettifiedName(Names.canonicalName(value));
  }

}
