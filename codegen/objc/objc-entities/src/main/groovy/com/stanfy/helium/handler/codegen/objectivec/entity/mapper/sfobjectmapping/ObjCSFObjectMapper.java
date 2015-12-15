package com.stanfy.helium.handler.codegen.objectivec.entity.mapper.sfobjectmapping;


import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCHeaderFile;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCImplementationFile;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject;
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjcEntitiesOptions;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClass;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClassDefinition;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCClassImplementation;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCPropertyDefinition;
import com.stanfy.helium.handler.codegen.objectivec.entity.mapper.ObjCMapper;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCImportPart;
import com.stanfy.helium.handler.codegen.objectivec.entity.file.ObjCStringSourcePart;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Project;

/**
 * Created by ptaykalo on 9/2/14.
 * Class that is responsible for generate files those are responsible for
 * correct mapping performing from JSON Objects to Messages
 * Generated classes will could be used with
 * https://github.com/stanfy/SFObjectMapping
 */
public class ObjCSFObjectMapper implements ObjCMapper {

  public static final String MAPPINGS_FILENAME = "HeliumMappings";

  @Override
  public void generateMappings(final ObjCProject project, final Project projectDSL, final ObjcEntitiesOptions options) {

    String className = options.getPrefix() + MAPPINGS_FILENAME;
    ObjCClass resultingClass = new ObjCClass(className);
    resultingClass.setDefinition(new ObjCClassDefinition(className));
    resultingClass.setImplementation(new ObjCClassImplementation(className));

    ObjCHeaderFile header = new ObjCHeaderFile(className);
    header.addSourcePart(resultingClass.getDefinition());

    ObjCImplementationFile implementation = new ObjCImplementationFile(className);
    implementation.addSourcePart(resultingClass.getImplementation());

    // Generate all them all
    for (Message m : projectDSL.getMessages()) {
      ObjCClass objCClass = project.getClassForType(m.getName());
      if (objCClass == null) {
        continue;
      }
      // get property definitions
      StringBuilder contentsBuilder = new StringBuilder();
      // Get the implementation
      contentsBuilder.append("+(void)initialize {").append("\n");
      contentsBuilder.append("    [self setMappingInfo:").append("\n");

      for (ObjCPropertyDefinition prop : objCClass.getDefinition().getPropertyDefinitions()) {
        contentsBuilder.append("      [SFMapping ");
        Field field = prop.getCorrespondingField();
        if (field != null) {
          if (field.isSequence()) {
            String itemClass = prop.getSequenceType();
            contentsBuilder.append("collection:@\"").append(prop.getName()).append("\" itemClass:@\"").append(itemClass).append("\" toKeyPath:@\"").append(field.getName()).append("\"],\n");
          } else {
            contentsBuilder.append("property:@\"").append(prop.getName()).append("\" toKeyPath:@\"").append(field.getName()).append("\"],\n");
          }

        } else {
          contentsBuilder.append("property:@\"").append(prop.getName()).append("\" toKeyPath:@\"").append(prop.getName()).append("\"],\n");
        }

      }
      contentsBuilder.append("    nil];").append("\n");
      contentsBuilder.append("}").append("\n");

      objCClass.getImplementation().addImportSourcePart(new ObjCImportPart("SFMapping"));
      objCClass.getImplementation().addImportSourcePart(new ObjCImportPart("NSObject+SFMapping"));

      objCClass.getImplementation().addBodySourcePart(new ObjCStringSourcePart(contentsBuilder.toString()));

    }
    project.addFile(header);
    project.addFile(implementation);
  }
}
