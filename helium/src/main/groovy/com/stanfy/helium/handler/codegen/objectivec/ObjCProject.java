package com.stanfy.helium.handler.codegen.objectivec;

import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClass;

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
  /*
  Classes that this project contains
   */
  private ArrayList<ObjCClass> classes = new ArrayList<ObjCClass>();

  public List<ObjCFile> getFiles() {
    return files;
  }

  public void addFile(ObjCFile file) {
    files.add(file);
  }

  public List<ObjCClass> getClasses() { return classes; }

  public void addClass(final ObjCClass objCClass) { classes.add(objCClass); }


}
