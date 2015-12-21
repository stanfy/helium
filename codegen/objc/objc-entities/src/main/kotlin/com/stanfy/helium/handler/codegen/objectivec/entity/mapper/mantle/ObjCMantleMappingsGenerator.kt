package com.stanfy.helium.handler.codegen.objectivec.entity.mapper.sfobjectmapping

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectStructureGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCMethod
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCMethodImplementationSourcePart
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCStringSourcePart
import com.stanfy.helium.model.Project

/**
 * Created by ptaykalo on 9/2/14.
 * Class that is responsible for generate files those are responsible for
 * correct mapping performing from JSON Objects to Messages
 * Generated classes will could be used with
 * https://github.com/stanfy/SFObjectMapping
 */
public class ObjCMantleMappingsGenerator : ObjCProjectStructureGenerator {
  override fun generate(project: ObjCProject, projectDSL: Project, options: ObjCEntitiesOptions) {


    // get property definitions
    // Generate all them all
    for (m in projectDSL.messages) {
      val objCClass = project.classStructure.getClassForType(m.name) ?: continue
      objCClass.definition.superClassName = "MTLModel"
      objCClass.definition.implementedProtocols.add("MTLJSONSerializing")
      objCClass.definition.importFrameworkWithName("Mantle")

//      """
//      + (NSDictionary *)JSONKeyPathsByPropertyKey {
//    return @{ @"affiliateId" : @"affiliate_id",
//              @"checkInDate" : @"checkin",
//             @"checkOutDate" : @"checkout",
//       @"confirmationNumber" : @"confirmation_num",
//               @"keychainId" : @"keychain_id",
//                 @"fullName" : @"full_name",
//         @"guestPhoneNumber" : @"phone",
//             @"modifiedDate" : @"modified",
//            @"lockInstalled" : @"lock_installed",
//           @"numberOfNights" : @"num_nights",
//            @"reservationId" : @"id",
//               @"roomNumber" : @"room_number",
//                 @"roomType" : @"room_type",
//                    @"state" : @"state",
//                    @"token" : @"token",
//                @"affiliate" : @"affiliate" };
//}
//
//      """
      val contentsBuilder = StringBuilder()

      val jsonMappingsMethod = ObjCMethod("JSONKeyPathsByPropertyKey", ObjCMethod.ObjCMethodType.CLASS, "NSDictionary *")
      val jsonMappingsMethodImpl = ObjCMethodImplementationSourcePart(jsonMappingsMethod)
      objCClass.implementation.addBodySourcePart(jsonMappingsMethodImpl)

      // Get the implementation
      contentsBuilder.append(" return @{\n")
      for (prop in objCClass.definition.propertyDefinitions) {
        val field = prop.correspondingField
        if (field != null) {
          if (field.isSequence) {
            val itemClass = prop.sequenceType!!.name
            var valueTransformerMethod = ObjCMethod(prop.name + "JSONTransformer",ObjCMethod.ObjCMethodType.CLASS, "NSValueTransformer *" )
            var valueTransformerMethodImpl = ObjCMethodImplementationSourcePart(valueTransformerMethod)
            valueTransformerMethodImpl.addSourcePart("""
            return [MTLValueTransformer mtl_JSONArrayTransformerWithModelClass:[$itemClass class]];
            """)
            objCClass.implementation.addBodySourcePart(valueTransformerMethodImpl)
            objCClass.implementation.importClassWithName(itemClass)

          } else {
            contentsBuilder.append(""" @"${prop.name}" : @"${field.name}",
            """)
          }

        } else {
          contentsBuilder.append(""" @"${prop.name}" : @"${prop.name}",
          """)
        }

      }

      contentsBuilder.append(" };")

      jsonMappingsMethodImpl.addSourcePart(ObjCStringSourcePart(contentsBuilder.toString()))

    }

  }
}
