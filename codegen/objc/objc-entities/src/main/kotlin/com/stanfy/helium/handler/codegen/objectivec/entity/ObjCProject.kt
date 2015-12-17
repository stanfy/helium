package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassesStructure
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCProjectFilesStructure

/**
 * Created by ptaykalo on 8/17/14.
 * Class that represents structure of objective C Project
 */
public class ObjCProject(val classStructure: ObjCProjectClassesStructure, val fileStructure: ObjCProjectFilesStructure)
