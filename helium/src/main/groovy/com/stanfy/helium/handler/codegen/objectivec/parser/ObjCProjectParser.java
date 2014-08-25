package com.stanfy.helium.handler.codegen.objectivec.parser;

import com.stanfy.helium.handler.codegen.objectivec.ObjCHeaderFile;
import com.stanfy.helium.handler.codegen.objectivec.ObjCImplementationFile;
import com.stanfy.helium.handler.codegen.objectivec.ObjCProject;
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClass;
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCPropertyDefinition;
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassDefinition;
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassImplementation;
import com.stanfy.helium.handler.codegen.objectivec.parser.options.ObjCProjectParserOptions;
import com.stanfy.helium.model.Field;
import com.stanfy.helium.model.Message;
import com.stanfy.helium.model.Project;

/**
 * Created by ptaykalo on 8/17/14.
 *
 */
public class ObjCProjectParser {


  /*
  Performs parsing / translation of Helium DSL Proejct Structure to Objective-C Project structure
   */
  public ObjCProject parse(final Project project) {
    return parse(project, null);
  }

  /*
  Performs parsing / translation of Helium DSL Proejct Structure to Objective-C Project structure
  Uses specified options for the generation @see ObjCProjectParserOptions
   */
  public ObjCProject parse(final Project project, final ObjCProjectParserOptions options) {
    ObjCProject objCProject = new ObjCProject();
    for (Message message : project.getMessages()) {

      String fileName = message.getName();
      if (options != null && options.getPrefix() != null) {
        fileName = options.getPrefix() + fileName;
      }

      ObjCClass objCClass = new ObjCClass(fileName);

      ObjCClassDefinition classDefinition = new ObjCClassDefinition(fileName);
      ObjCClassImplementation classImplementation = new ObjCClassImplementation(fileName);

      for (Field field : message.getActiveFields()) {
        classDefinition.addPropertyDefinition( new ObjCPropertyDefinition(field.getName(), field.getType().getName()));
      }

      objCClass.setDefinition(classDefinition);
      objCClass.setImplementation(classImplementation);

      objCProject.addClass(objCClass);
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
