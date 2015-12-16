package com.stanfy.helium.gradle.tasks;

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesGenerator;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions;

/**
 * Generates Obj-C code (in dev).
 */
public class GenerateObjcEntitiesTask extends BaseHeliumTask<ObjCEntitiesOptions> {

  @Override
  protected void doIt() {
    getHelium().processBy(new ObjCEntitiesGenerator(getOutput(), getOptions()));
  }

}
