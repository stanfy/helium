package com.stanfy.helium.handler.codegen.objectivec.entity.filetree


/**
 * Created by paultaykalo on 12/17/15.
 */

public class ObjCProjectFilesStructure {

  /**
   * Files those are contained in this project. Each should have an unique name
   */
  public val files = arrayListOf<ObjCFile>()

  /**
   * Adds file to the project structure
   */
  public fun addFile(file: ObjCFile) {
    files.add(file);
  }

  public fun <T> addFiles(filesList: List<T>) where T:ObjCFile {
    files.addAll(filesList);
  }


}