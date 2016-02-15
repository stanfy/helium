package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassesStructure
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCImplementationFile
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCProjectFilesStructure

/**
 * Created by paultaykalo on 12/17/15.
 * Transforms classes tree to the structure of source files
 */

class ObjCDefaultFileStructureBuilder : ObjCBuilder<ObjCProjectClassesStructure, ObjCProjectFilesStructure> {

  override fun build(from: ObjCProjectClassesStructure, options: ObjCEntitiesOptions?): ObjCProjectFilesStructure {
    val result = ObjCProjectFilesStructure()
    result.addFiles(
        from.classes.flatMap { objcClass ->
          val headerBuilder = ObjCHeaderFileBuilder()
          val headerFile = ObjCHeaderFile(objcClass.name, headerBuilder.build(objcClass, options))
          val implementationFile = ObjCImplementationFile(objcClass.name, objcClass.implementation.asString())
          listOf(headerFile, implementationFile)
        }
    )

    result.addFiles(
        from.pregeneratedClasses
            .filter { objClass -> objClass.header != null }
            .map { objcClass -> ObjCHeaderFile(objcClass.name, objcClass.header!!) }
    )

    result.addFiles(
        from.pregeneratedClasses
            .filter { objClass -> objClass.implementation != null }
            .map { objcClass -> ObjCImplementationFile(objcClass.name, objcClass.implementation!!) }
    )

    return result
  }


}