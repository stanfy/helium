package com.stanfy.helium.handler.codegen.objectivec.entity.builder;

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.*
import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCProperty
import com.stanfy.helium.handler.codegen.objectivec.entity.model.ObjCType
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project
import com.stanfy.helium.model.Sequence

/**
 * Created by ptaykalo on 8/17/14.
 */
@Deprecated("User ObjcProjectBuilder instead")
class ObjCDefaultClassStructureBuilder(val typeTransformer: ObjCTypeTransformer,
                                       val nameTransformer: ObjCPropertyNameTransformer) : ObjCBuilder<Project, ObjCProjectClassesStructure> {

  /**
   * Performs parsing / translation of Helium DSL Project Structure to Objective-C Project structure
   * Uses specified options for the generation @see ObjCProjectParserOptions
   */
  override fun build(from: Project, options: ObjCEntitiesOptions?): ObjCProjectClassesStructure {
//    val projectClassStructure = ObjCProjectClassesTree()
//
//    val filteredMessages =
//        from.messages.filter { message ->
//          !message.anonymous && (options == null || options.isTypeIncluded(message))
//        }
//
//    // Registering all direct transformations from messages
//    typeTransformer.registerTransformations(
//        filteredMessages.map { message ->
//          val className = (options?.prefix ?: "") + message.name
//          ObjCTypeTransformation(message.name, ObjCType(className), ObjCProperty.AccessModifier.STRONG)
//        })
//
//    // Register custom transformations
//    val customTypeMappings = options?.customTypesMappings?.entries
//    if (customTypeMappings != null) {
//      typeTransformer.registerTransformations(
//          customTypeMappings.map { e ->
//            val heliumType = e.key
//            val objcType = e.value
//            val isReference = objcType.contains("*")
//            val name = objcType.replace(" ", "").replace("*", "")
//            val accessModifier = if (isReference || name == "id") ObjCProperty.AccessModifier.STRONG else ObjCProperty.AccessModifier.ASSIGN
//            ObjCTypeTransformation(heliumType, ObjCType(name, isReference, true), accessModifier)
//          }
//      )
//    }
//
//    filteredMessages.forEach { message ->
//      projectClassStructure.addClass(objCClassForMessage(message, classPrefix = options?.prefix ?: ""), message.name);
//    }
//
//    val filteredSequences =
//      from.sequences.filter { seq ->
//        !seq.isAnonymous && (options == null || options.isTypeIncluded(seq))
//      }
//
//    // Registering all direct transformations from sequences
//    typeTransformer.registerTransformations(
//        filteredSequences.map { seq ->
//          val className = (options?.prefix ?: "") + seq.name
//          ObjCTypeTransformation(seq.name, ObjCType(className), ObjCProperty.AccessModifier.STRONG)
//        })
//
//    filteredSequences.forEach { seq ->
//      projectClassStructure.addClass(objcClassForSequence(seq, classPrefix = options?.prefix ?: ""), seq.name);
//    }
//
//    return projectClassStructure
    return ObjCProjectClassesStructure()
  }

//  private fun objcClassForSequence(seq: Sequence?, classPrefix: String): ObjCComplexClass {
//    val className = classPrefix + seq!!.name
//    val classDefinition = ObjCClassInterface(className)
//    val classImplementation = ObjCClassImplementation(className)
//    val objCClass = ObjCComplexClass(className, classDefinition, classImplementation)
//    return objCClass
//  }

//  private fun objCClassForMessage(message: Message, classPrefix: String): ObjCComplexClass {
//    val className = classPrefix + message.name
//    val classDefinition = ObjCClassInterface(className)
//    val classImplementation = ObjCClassImplementation(className)
//    val objCClass = ObjCComplexClass(className, classDefinition, classImplementation)
//
//    val usedPropertyNames = hashSetOf<String>()
//
//    // Transform fields to the objc properties
//    classDefinition.addPropertyDefinitionsList(
//        message.activeFields
//            .map { field ->
//              val heliumType = field.type
//              val propertyName = nameTransformer.propertyNameFrom(field.name, usedPropertyNames)
//
//              val propertyType = typeTransformer.objCType(heliumType, field.isSequence)
//              val accessModifier = typeTransformer.accessorModifierForType(heliumType)
//
//              val property = ObjCProperty(propertyName, propertyType, accessModifier)
//              property.correspondingField = field
//
//              if (field.isSequence) {
//                val itemType = typeTransformer.objCType(heliumType, false)
//                property.isSequence = true
//                property.sequenceType = itemType
//              }
//
//              // Update used Names
//              usedPropertyNames.add(propertyName)
//
//              property
//            }
//    )
//
//    // Add forward declarations for property types
//    objCClass.addClassForwardDeclarations(
//        classDefinition.propertyDefinitions
//            .filter { prop -> prop.type.isReference && !prop.type.isFoundationType() }
//            .map { prop -> prop.type.name }
//    )
//
//    // Add forward declarations for property sequence types
//    objCClass.addClassForwardDeclarations(
//        classDefinition.propertyDefinitions
//            .filter { prop -> prop.isSequence && prop.sequenceType != null && prop.sequenceType!!.isReference && !prop.sequenceType!!.isFoundationType() }
//            .map { prop -> prop.sequenceType!!.name }
//    )
//
//    return objCClass
//  }
}
