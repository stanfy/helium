package com.stanfy.helium.handler.codegen.objectivec;

import com.stanfy.helium.handler.Handler;
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
      objCProject.addFile(new ObjCHeaderFile(message.getName()));
      objCProject.addFile(new ObjCImplementationFile(message.getName()));
    }
    return objCProject;
  }

}
