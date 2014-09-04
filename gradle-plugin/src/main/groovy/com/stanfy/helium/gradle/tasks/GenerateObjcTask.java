package com.stanfy.helium.gradle.tasks;

import com.stanfy.helium.handler.codegen.objectivec.ObjCProjectHandler;
import com.stanfy.helium.handler.codegen.objectivec.parser.options.ObjCProjectParserOptions;

/**
 * Generates Obj-C code (in dev).
 */
public class GenerateObjcTask extends BaseHeliumTask<ObjCProjectParserOptions> {

  @Override
  protected void doIt() {
    getHelium().processBy(new ObjCProjectHandler(getOutput(), getOptions()));
  }

}
