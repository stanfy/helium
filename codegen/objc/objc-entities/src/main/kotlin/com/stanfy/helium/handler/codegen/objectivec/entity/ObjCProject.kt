package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassStructure
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCFile
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCClass;
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCProjectFileStructure

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ptaykalo on 8/17/14.
 * Class that represents structure of objective C Project
 */
public class ObjCProject {

  /**
   * Project's file structure
   */
  public val fileStructure = ObjCProjectFileStructure()

  /**
   * Projects class structure
   */
  public val classStructure = ObjCProjectClassStructure()
}
