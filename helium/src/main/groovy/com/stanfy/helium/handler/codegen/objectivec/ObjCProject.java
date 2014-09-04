package com.stanfy.helium.handler.codegen.objectivec;

import com.stanfy.helium.handler.codegen.objectivec.file.ObjCClass;

import java.util.ArrayList;
import java.util.HashMap;
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
  /*
  Holds mapping form DSL Type names to classes, that this proejct contains
   */
  private HashMap<String, ObjCClass> classesByTypes = new HashMap<String, ObjCClass>();

  public List<ObjCFile> getFiles() {
    return files;
  }

  public void addFile(final ObjCFile file) {
    files.add(file);
  }

  public List<ObjCClass> getClasses() { return classes; }

  public void addClass(final ObjCClass objCClass) { classes.add(objCClass); }

  /*
  Adds class, and bounds it to the specified DSL Type
   */
  public void addClass(final ObjCClass objCClass, final String dslType) {
    classes.add(objCClass);
    classesByTypes.put(dslType, objCClass);
  }

  public ObjCClass getClassForType(final String dslTypeName) {
    return classesByTypes.get(dslTypeName);
  }
}
