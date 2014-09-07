package com.stanfy.helium.handler.codegen.objectivec;

import com.stanfy.helium.handler.codegen.GeneratorOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Options for a handler that generated Obj-C entities.
 */
public class ObjcEntitiesOptions extends GeneratorOptions {

  /** Class names prefix. */
  private String prefix = "HE";

  /** Map that contains mappings for custom Helium Types. i.e. timestamp -> NSDate.
   * It is used for generating custom(complex) types.
   * */
  private Map<String, String> customTypesMappings =  new HashMap<String, String>();

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  /** Returns prefix for all files those would be generated */
  public String getPrefix() {
    return prefix;
  }


  public void setCustomTypesMappings(final Map<String, String> customTypesMappings) {
    this.customTypesMappings = customTypesMappings;
  }

  public Map<String, String> getCustomTypesMappings() {
    return customTypesMappings;
  }
}
