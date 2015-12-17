package com.stanfy.helium.handler.codegen.objectivec.entity.builder;

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCHeaderFile
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCImplementationFile
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClass
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClassInterface
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCImplementationFileSourcePart
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCPropertyDefinition
import com.stanfy.helium.model.Message
import com.stanfy.helium.model.Project

/**
 * Created by ptaykalo on 8/17/14.
 */
public class DefaultObjCProjectBuilder : ObjCProjectBuilder {

  private val _nameTransformer = ObjCPropertyNameTransformer()
  override fun getNameTransformer(): ObjCPropertyNameTransformer {
    return _nameTransformer;
  }

  private val _typeTransformer = ObjCTypeTransformer()
  override fun getTypeTransformer(): ObjCTypeTransformer {
    return _typeTransformer
  }

  /**
   * Performs parsing / translation of Helium DSL Project Structure to Objective-C Project structure
   */
  override fun build(project: Project): ObjCProject {
    return build(project, null)
  }

  /**
   * Performs parsing / translation of Helium DSL Project Structure to Objective-C Project structure
   * Uses specified options for the generation @see ObjCProjectParserOptions
   */
  override fun build(project: Project, options: ObjCEntitiesOptions?): ObjCProject {
    val objCProject = ObjCProject()

    project.messages
        .filter { message ->
          !message.anonymous && (options == null || options.isTypeIncluded(message))
        }
        .forEach { message ->
          val messageName = message.name
          val className = (options?.prefix ?: "") + messageName
          _typeTransformer.registerRefTypeTransformation(messageName, className)

          // check for custom mappings
          val customTypeMappings = options?.customTypesMappings?.entries
          if (customTypeMappings != null) {
            for ((heliumType, objcType) in customTypeMappings) {
              if (objcType.contains("*")) {
                val validObjectiveCString = objcType.replace("*", "").trim()
                _typeTransformer.registerRefTypeTransformation(heliumType, validObjectiveCString);
              } else {
                _typeTransformer.registerSimpleTransformation(heliumType, objcType);
              }
            }
          }
        }

    project.messages
        .forEach { message ->
          val filename = (options?.prefix ?: "") + message.name
          val objCClass = ObjCClass(filename)

          val classDefinition = ObjCClassInterface(filename)
          val classImplementation = ObjCImplementationFileSourcePart(filename)

          var usedPropertyNames = hashSetOf<String>()
          message.activeFields
              .map { field ->
                val propertyName = _nameTransformer.propertyNameFrom(field.name, usedPropertyNames)
                val heliumAPIType = field.type
                val propertyType = _typeTransformer.objCType(heliumAPIType, field.isSequence)
                if (heliumAPIType is Message && !field.isSequence) {
                  classDefinition.addExternalClassDeclaration(propertyType.replace("*", "").replace(" ",""));
                }

                val accessModifier = _typeTransformer.accessorModifierForType(heliumAPIType)
                val property = ObjCPropertyDefinition(propertyName, propertyType, accessModifier)
                property.correspondingField = field

                if (field.isSequence) {
                  property.comment = " sequence of " + _typeTransformer.objCType(heliumAPIType, false) + " items"
                  property.isSequence = true
                  property.sequenceType = _typeTransformer.objCType(heliumAPIType, false).replace("*", "").replace(" ","")
                }
                // Update used Names
                usedPropertyNames.add(propertyName)

                property

              }
              .forEach { property ->
                classDefinition.addPropertyDefinition(property);
              }

          objCClass.definition = classDefinition;
          objCClass.implementation = classImplementation;

          objCProject.addClass(objCClass, message.name);
          val headerFile = ObjCHeaderFile(filename);
          val implementationFile = ObjCImplementationFile(filename);
          headerFile.addSourcePart(classDefinition);
          implementationFile.addSourcePart(classImplementation);

          objCProject.addFile(headerFile);
          objCProject.addFile(implementationFile);
        }

    return objCProject
  }
}
