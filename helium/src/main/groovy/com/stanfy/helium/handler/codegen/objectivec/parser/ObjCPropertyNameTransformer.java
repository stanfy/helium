package com.stanfy.helium.handler.codegen.objectivec.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by ptaykalo on 8/25/14.
 * Transform property names for specified class
 * It updateing words those could possibly be
 */
public class ObjCPropertyNameTransformer {

  //http://www.binpress.com/tutorial/objective-c-reserved-keywords/43
  private static Set<String> KEYWORDS = new HashSet<String>(Arrays.asList("auto", "break", "case", "char", "const", "continue", "default", "do", "double",
      "else", "enum", "extern", "float", "for", "goto", "if", "inline", "int", "long", "register", "restrict", "return",
      "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while",
      "_Bool", "_Complex", "_Imaginery", "BOOL", "Class", "bycopy", "byref", "id", "IMP", "in", "inout", "nil", "NO", "NULL",
      "oneway", "out", "Protocol", "SEL", "self", "super", "YES", "interface", "end", "implementation", "protocol", "class",
      "public", "protected", "private", "property", "try", "throw", "catch()", "finally", "synthesize", "dynamic",
      "selector", "atomic", "nonatomic", "retain"));


  public String propertyNameFrom(final String propertyName) {
    return propertyNameFrom(propertyName, null);
  }

  public String propertyNameFrom(final String propertyName, final Set<String> nonAllowedNames) {
    Set<String> st = KEYWORDS;

    // update Property name :)
    String currPropertyName = propertyName;
    if (KEYWORDS.contains(currPropertyName)) {
      currPropertyName = currPropertyName + "_Field";
    } else if (nonAllowedNames != null && nonAllowedNames.contains(currPropertyName)) {
      currPropertyName = currPropertyName + "_Field";
    }

    return toCamelCase(currPropertyName);
  }

  private static String toCamelCase(String value) {
    StringBuilder sb = new StringBuilder();

    final char delimChar = '_';
    boolean lower = false;

    int startIndex = value.indexOf(delimChar);
    if (startIndex == -1) {
      return value;
    }
    sb.append(value.substring(0, startIndex));
    for (int charInd = startIndex + 1; charInd < value.length(); ++charInd) {
      final char valueChar = value.charAt(charInd);
      if (valueChar == delimChar) {
        lower = false;
      } else if (lower) {
        sb.append(Character.toLowerCase(valueChar));
      } else {
        sb.append(Character.toUpperCase(valueChar));
        lower = true;
      }
    }

    return sb.toString();
  }

}
