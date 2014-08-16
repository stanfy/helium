package com.stanfy.helium.handler.codegen.objectivec;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ptaykalo on 8/17/14.
 * Class that represents structure of objective C Project
 */
public class ObjCProject {

  /*
  Files those are contained in this project. Each should have an unique name
   */
  private ArrayList<ObjCFile> files = new ArrayList<ObjCFile>();

  List<ObjCFile> getFiles() {
    return files;
  }

  void addFile(ObjCFile file) {
    files.add(file);
  }

}
