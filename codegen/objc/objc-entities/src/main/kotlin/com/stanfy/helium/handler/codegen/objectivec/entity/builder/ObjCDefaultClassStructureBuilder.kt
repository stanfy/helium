package com.stanfy.helium.handler.codegen.objectivec.entity.builder;

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassesStructure
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCClass
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCClassImplementation
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCClassInterface
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCPropertyDefinition
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project

/**
 * Created by ptaykalo on 8/17/14.
 */
public class ObjCDefaultClassStructureBuilder : ObjCClassStructureBuilder {

  public val nameTransformer = ObjCPropertyNameTransformer()

  public val typeTransformer = ObjCTypeTransformer()

  /**
   * Performs parsing / translation of Helium DSL Project Structure to Objective-C Project structure
   */
  override fun build(from: Project): ObjCProjectClassesStructure {
    return build(from, null)
  }

  /**
   * Performs parsing / translation of Helium DSL Project Structure to Objective-C Project structure
   * Uses specified options for the generation @see ObjCProjectParserOptions
   */
  override fun build(from: Project, options: ObjCEntitiesOptions?): ObjCProjectClassesStructure {
    val projectClassStructure = ObjCProjectClassesStructure()

    from.messages
        .filter { message ->
          !message.anonymous && (options == null || options.isTypeIncluded(message))
        }
        .forEach { message ->
          val messageName = message.name
          val className = (options?.prefix ?: "") + messageName
          typeTransformer.registerRefTypeTransformation(messageName, className)

          // check for custom mappings
          val customTypeMappings = options?.customTypesMappings?.entries
          if (customTypeMappings != null) {
            for ((heliumType, objcType) in customTypeMappings) {
              if (objcType.contains("*")) {
                val validObjectiveCString = objcType.replace("*", "").trim()
                typeTransformer.registerRefTypeTransformation(heliumType, validObjectiveCString);
              } else {
                typeTransformer.registerSimpleTransformation(heliumType, objcType);
              }
            }
          }
        }

    from.messages
        .forEach { message ->
          val className = (options?.prefix ?: "") + message.name
          val classDefinition = ObjCClassInterface(className)
          val classImplementation = ObjCClassImplementation(className)
          val objCClass = ObjCClass(className, classDefinition, classImplementation)

          var usedPropertyNames = hashSetOf<String>()
          message.activeFields
              .map { field ->
                val propertyName = nameTransformer.propertyNameFrom(field.name, usedPropertyNames)
                val heliumAPIType = field.type
                val propertyType = typeTransformer.objCType(heliumAPIType, field.isSequence)
                if (heliumAPIType is Message && !field.isSequence) {
                  classDefinition.addExternalClassDeclaration(propertyType.replace("*", "").replace(" ", ""));
                }

                val accessModifier = typeTransformer.accessorModifierForType(heliumAPIType)
                val property = ObjCPropertyDefinition(propertyName, propertyType, accessModifier)
                property.correspondingField = field

                if (field.isSequence) {
                  property.comment = " sequence of " + typeTransformer.objCType(heliumAPIType, false) + " items"
                  property.isSequence = true
                  property.sequenceType = typeTransformer.objCType(heliumAPIType, false).replace("*", "").replace(" ", "")
                }
                // Update used Names
                usedPropertyNames.add(propertyName)

                property

              }
              .forEach { property ->
                classDefinition.addPropertyDefinition(property);
              }

          projectClassStructure.addClass(objCClass, message.name);
        }

    return projectClassStructure
  }
}
