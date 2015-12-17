package com.stanfy.helium.handler.codegen.objectivec.entity.mapper.sfobjectmapping

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCImplementationFile
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.file.*
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCMethodImplementationSourcePart.ObjCMethodType
import com.stanfy.helium.handler.codegen.objectivec.entity.mapper.ObjCMapper
import com.stanfy.helium.model.Project

/**
 * Created by ptaykalo on 9/2/14.
 * Class that is responsible for generate files those are responsible for
 * correct mapping performing from JSON Objects to Messages
 * Generated classes will could be used with
 * https://github.com/stanfy/SFObjectMapping
 */
public class ObjCSFObjectMapper : ObjCMapper {
  public val MAPPINGS_FILENAME = "HeliumMappings"

  override fun generateMappings(project: ObjCProject, projectDSL: Project, options: ObjCEntitiesOptions) {
    val className = options.prefix + MAPPINGS_FILENAME
    val resultingClass = ObjCClass(className)
    resultingClass.definition = ObjCClassInterface(className)
    resultingClass.implementation = ObjCImplementationFileSourcePart(className)

    val header = ObjCHeaderFile(className)
    header.addSourcePart(resultingClass.definition as ObjCClassInterface)

    val implementation = ObjCImplementationFile(className)
    implementation.addSourcePart(resultingClass.implementation as ObjCImplementationFileSourcePart)

    // Generate all them all
    for (m in projectDSL.messages) {
      val objCClass = project.getClassForType(m.name) ?: continue

      val initializeMethod = ObjCMethodImplementationSourcePart("initialize", ObjCMethodType.CLASS, "void")
      // get property definitions
      val contentsBuilder = StringBuilder()
      // Get the implementation
      contentsBuilder.append("    [self setMappingInfo:").append("\n")

      for (prop in objCClass.definition!!.propertyDefinitions) {
        contentsBuilder.append("      [SFMapping ")
        val field = prop.correspondingField
        if (field != null) {
          if (field.isSequence) {
            val itemClass = prop.sequenceType
            contentsBuilder.append("collection:@\"").append(prop.name).append("\" itemClass:@\"").append(itemClass).append("\" toKeyPath:@\"").append(field.name).append("\"],\n")
          } else {
            contentsBuilder.append("property:@\"").append(prop.name).append("\" toKeyPath:@\"").append(field.name).append("\"],\n")
          }

        } else {
          contentsBuilder.append("property:@\"").append(prop.name).append("\" toKeyPath:@\"").append(prop.name).append("\"],\n")
        }

      }
      contentsBuilder.append("    nil]").append("\n")
      initializeMethod.addSourcePart(ObjCStringSourcePart(contentsBuilder.toString()))

      val implementationfile = objCClass.implementation!!
      implementationfile.addImportSourcePart(ObjCImportPart("SFMapping"))
      implementationfile.addImportSourcePart(ObjCImportPart("NSObject+SFMapping"))

      implementationfile.addBodySourcePart(initializeMethod)

    }
    project.addFile(header)
    project.addFile(implementation)

  }

}
