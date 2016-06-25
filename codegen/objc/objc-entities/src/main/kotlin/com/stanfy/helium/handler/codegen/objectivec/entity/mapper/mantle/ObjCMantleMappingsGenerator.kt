package com.stanfy.helium.handler.codegen.objectivec.entity.mapper.mantle

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectComplex
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectStructureGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCComplexClass
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCMethod
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCMethodImplementationSourcePart
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCPregeneratedClass
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCSourcePart
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCStringSourcePart
import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCProject
import com.stanfy.helium.model.Project

/**
 * Created by ptaykalo on 9/2/14.
 * Class that is responsible for generate files those are responsible for
 * correct mapping performing from JSON Objects to Messages
 * Generated classes will could be used with
 * https://github.com/Mantle/Mantle
 */
public class ObjCMantleMappingsGenerator : ObjCProjectStructureGenerator {
  override fun generate(project: ObjCProject, projectDSL: Project, options: ObjCEntitiesOptions) {

//    val messages = projectDSL.messages
//    val objcClasses = messages.mapNotNull { m -> project.classesTree.getClassForType(m.name) }
//
//    objcClasses.forEach { objCClass ->
//
//      objCClass.definition.superClassName = "MTLModel"
//      objCClass.definition.implementedProtocols.add("MTLJSONSerializing")
//      objCClass.definition.importFrameworkWithName("Mantle/Mantle")
//
//      generateKeyPathsByPropertyKeyMethod(objCClass, options)
//    }
//
//    // Generate protocol for custom mapping
//    addCustomMantleValueTransformerProtocol(project, options)
//  }
//
//  private fun generateKeyPathsByPropertyKeyMethod(objCClass: ObjCComplexClass, options: ObjCEntitiesOptions): ObjCSourcePart {
//    val contentsBuilder = StringBuilder()
//
//    val jsonMappingsMethod = ObjCMethod("JSONKeyPathsByPropertyKey", ObjCMethod.ObjCMethodType.CLASS, "NSDictionary *")
//    val jsonMappingsMethodImpl = ObjCMethodImplementationSourcePart(jsonMappingsMethod)
//
//    objCClass.implementation.addBodySourcePart(jsonMappingsMethodImpl)
//
//    val customValueTransformerProtocolName = options.prefix + "CustomValueTransformerProtocol"
//
//    // Get the implementation
//    contentsBuilder.append(" return @{\n")
//
//    // Simple values with no corresponding field
//    val propertyDefinitions = objCClass.definition.propertyDefinitions
//
//    propertyDefinitions
//        .forEach { prop ->
//          val heliumField = prop.correspondingField
//
//          // We don't have meta information, so we just
//          // setting that there' no mapping can be perfomed
//          if (heliumField == null) {
//            contentsBuilder.append(""" @"${prop.name}" : [NSNull null],
//            """)
//            return@forEach
//          }
//
//          // If it's a sequence, then we '' add propertyJSONTranfromer for it
//          if (heliumField.isSequence) {
//            val itemClass = prop.sequenceType!!.name
//            var valueTransformerMethod = ObjCMethod(prop.name + "JSONTransformer", ObjCMethod.ObjCMethodType.CLASS, "NSValueTransformer *")
//            var valueTransformerMethodImpl = ObjCMethodImplementationSourcePart(valueTransformerMethod)
//            valueTransformerMethodImpl.addSourcePart("""
//            return [MTLValueTransformer mtl_JSONArrayTransformerWithModelClass:[$itemClass class]];
//            """)
//            objCClass.implementation.addBodySourcePart(valueTransformerMethodImpl)
//            if (!prop.sequenceType!!.isFoundationType()) {
//              objCClass.implementation.importClassWithName(itemClass)
//            }
//            return@forEach
//          }
//
//          // If it's foundation type it's simple property name -> JSON parameter name
//          if (prop.type.isFoundationType()) {
//            contentsBuilder.append(""" @"${prop.name}" : @"${heliumField.name}",
//            """)
//            return@forEach
//          }
//
//          //  Most general case
//          val propClass = prop.type.name
//          var valueTransformerMethod = ObjCMethod(prop.name + "JSONTransformer", ObjCMethod.ObjCMethodType.CLASS, "NSValueTransformer *")
//          var valueTransformerMethodImpl = ObjCMethodImplementationSourcePart(valueTransformerMethod)
//          val customValueTransformer = options.mantleCustomValueTransformers[propClass]
//          var propClassString = if (prop.type.isCustom) {
//            "NSClassFromString(@\"$propClass\")"
//          } else {
//            objCClass.implementation.importClassWithName(propClass)
//            "[$propClass class]"
//          }
//          if (customValueTransformer != null) {
//            objCClass.implementation.importClassWithName(customValueTransformerProtocolName)
//            valueTransformerMethodImpl.addSourcePart("""
//              return [(id<$customValueTransformerProtocolName>)NSClassFromString(@"$customValueTransformer") valueTransformerWithModelOfClass:$propClassString];
//             """)
//          } else {
//            valueTransformerMethodImpl.addSourcePart("""
//            return [MTLValueTransformer mtl_JSONDictionaryTransformerWithModelClass:$propClassString];
//            """)
//          }
//          objCClass.implementation.addBodySourcePart(valueTransformerMethodImpl)
//          contentsBuilder.append(""" @"${prop.name}" : @"${heliumField.name}",
//            """)
//        }
//
//    contentsBuilder.append(" };")
//    jsonMappingsMethodImpl.addSourcePart(ObjCStringSourcePart(contentsBuilder.toString()))
//    return  jsonMappingsMethodImpl
//  }
//
//  private fun addCustomMantleValueTransformerProtocol(project: ObjCProject, options: ObjCEntitiesOptions) {
//    val protocolName = options.prefix + "CustomValueTransformerProtocol"
//    project.classesTree.addSourceCodeClass(ObjCPregeneratedClass(protocolName, header =
//        """
//@protocol $protocolName <NSObject>
//+ (NSValueTransformer*)valueTransformerWithModelOfClass:(Class)modelClass;
//@end
//        """
//    , implementation = null));
  }
}
