package com.stanfy.helium.handler.codegen.objectivec;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClass;
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassDefinition;
import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClassImplementation;
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
    ObjCProject objCProject = new ObjCProject();
    for (Message message : project.getMessages()) {

      ObjCClass objCClass = new ObjCClass(message.getName());
      objCClass.setDefinition(new ObjCClassDefinition(message.getName()));
      objCClass.setImplementation(new ObjCClassImplementation(message.getName()));

      objCProject.addClass(objCClass);
      objCProject.addFile(new ObjCHeaderFile(message.getName()));
      objCProject.addFile(new ObjCImplementationFile(message.getName()));
    }

    // classes
    return objCProject;
  }

}
