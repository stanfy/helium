package com.stanfy.helium.handler.codegen.objectivec.entity.builder;

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassesStructure
import com.stanfy.helium.model.Project

/**
 * Created by ptaykalo on 9/2/14.
 * Interface for the class that can generate valid ObjCProject structure, based on provided
 * Helium DSL Project description
 */
public interface ObjCClassStructureBuilder : ObjCBuilder<Project, ObjCProjectClassesStructure> {

}
