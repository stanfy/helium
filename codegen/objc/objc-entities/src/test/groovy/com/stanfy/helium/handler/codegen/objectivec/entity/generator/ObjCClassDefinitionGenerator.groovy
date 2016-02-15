package com.stanfy.helium.handler.codegen.objectivec.entity.generator

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCClassInterface
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/25/14.
 */
class ObjCClassDefinitionGenerator extends Specification {

  ObjCClassInterface classInterface;

  def setup() {
    classInterface = new ObjCClassInterface("S");
  }

}

