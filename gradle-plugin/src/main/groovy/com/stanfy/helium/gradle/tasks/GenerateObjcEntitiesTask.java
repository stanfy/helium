package com.stanfy.helium.gradle.tasks;

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesGenerator;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjcEntitiesOptions;

/**
 * Generates Obj-C code (in dev).
 */
public class GenerateObjcEntitiesTask extends BaseHeliumTask<ObjcEntitiesOptions> {

  @Override
  protected void doIt() {
    getHelium().processBy(new ObjCEntitiesGenerator(getOutput(), getOptions()));
  }

}
