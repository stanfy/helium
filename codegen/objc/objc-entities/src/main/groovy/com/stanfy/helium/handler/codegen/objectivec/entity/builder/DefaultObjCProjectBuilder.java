package com.stanfy.helium.handler.codegen.objectivec.entity.builder;

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCHeaderFile;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCImplementationFile;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjcEntitiesOptions;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClass;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClassDefinition;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClassImplementation;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCPropertyDefinition;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Project;
import com.stanfy.helium.model.Type;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by ptaykalo on 8/17/14.
 *
 */
public class DefaultObjCProjectBuilder implements ObjCProjectBuilder {

  /*
     Type transformer to transform correct Objc types from Helium API
     */
  private ObjCTypeTransformer typeTransformer = new ObjCTypeTransformer();
  private ObjCPropertyNameTransformer nameTransformer = new ObjCPropertyNameTransformer();

  @Override
  public ObjCTypeTransformer getTypeTransformer() {
    return typeTransformer;
  }

  @Override
  public ObjCPropertyNameTransformer getNameTransformer() {
    return nameTransformer;
  }

  /*
  Performs parsing / translation of Helium DSL Proejct Structure to Objective-C Project structure
   */
  @Override
  public ObjCProject build(final Project project) {
    return build(project, null);
  }

  /*
  Performs parsing / translation of Helium DSL Proejct Structure to Objective-C Project structure
  Uses specified options for the generation @see ObjCProjectParserOptions
   */
  @Override
  public ObjCProject build(final Project project, final ObjcEntitiesOptions options) {
    ObjCProject objCProject = new ObjCProject();

    // Register all messages first
    for (Message message : project.getMessages()) {
      if (message.isAnonymous() || (options != null && !options.isTypeIncluded(message))) {
        continue;
      }
      String messageName = message.getName();
      String className = messageName;
      if (options != null && options.getPrefix() != null) {
        className = options.getPrefix() + className;
      }
      typeTransformer.registerRefTypeTransformation(messageName, className);
    }

    // Registering all custom mappings
    if (options != null && options.getCustomTypesMappings() != null) {
      Map<String, String> customTypesMappings = options.getCustomTypesMappings();
      for (Map.Entry<String, String> entry : customTypesMappings.entrySet()) {
        // Check if Objective-C Type have * here
        String objectiveCType = entry.getValue();
        String heliumType = entry.getKey();
        if (objectiveCType.contains("*")) {
          String validObjectiveCString = objectiveCType.replace("*", "").trim();
          typeTransformer.registerRefTypeTransformation(heliumType, validObjectiveCString);
        } else {
          typeTransformer.registerSimpleTransformation(heliumType, objectiveCType);
        }
      }
    }

    for (Message message : project.getMessages()) {

      String fileName = message.getName();
      if (options != null && options.getPrefix() != null) {
        fileName = options.getPrefix() + fileName;
      }

      ObjCClass objCClass = new ObjCClass(fileName);

      ObjCClassDefinition classDefinition = new ObjCClassDefinition(fileName);
      ObjCClassImplementation classImplementation = new ObjCClassImplementation(fileName);

      HashSet<String> usedPropertyNames = new HashSet<String>();
      for (Field field : message.getActiveFields()) {
        String propertyName = nameTransformer.propertyNameFrom(field.getName(), usedPropertyNames);
        Type heliumAPIType = field.getType();
        String propertyType = typeTransformer.objCType(heliumAPIType, field.isSequence());

        if (heliumAPIType instanceof Message && !field.isSequence()) {
          classDefinition.addExternalClassDeclaration(propertyType.replaceAll("\\*|\\s", ""));
        }
        ObjCPropertyDefinition.AccessModifier accessModifier = typeTransformer.accessorModifierForType(heliumAPIType);
        ObjCPropertyDefinition property = new ObjCPropertyDefinition(propertyName, propertyType, accessModifier);
        property.setCorrespondingField(field);

        if (field.isSequence()) {
          property.setComment(" sequence of " + typeTransformer.objCType(heliumAPIType, false) + " items");
          property.setSequence(true);
          property.setSequenceType(typeTransformer.objCType(heliumAPIType, false).replaceAll("\\*|\\s", ""));
        }

        classDefinition.addPropertyDefinition(property);

        // Update used Names
        usedPropertyNames.add(propertyName);
      }

      objCClass.setDefinition(classDefinition);
      objCClass.setImplementation(classImplementation);

      objCProject.addClass(objCClass, message.getName());
      ObjCHeaderFile headerFile = new ObjCHeaderFile(fileName);
      ObjCImplementationFile implementationFile = new ObjCImplementationFile(fileName);
      headerFile.addSourcePart(classDefinition);
      implementationFile.addSourcePart(classImplementation);

      objCProject.addFile(headerFile);
      objCProject.addFile(implementationFile);
    }

    // classes
    return objCProject;
  }

}
