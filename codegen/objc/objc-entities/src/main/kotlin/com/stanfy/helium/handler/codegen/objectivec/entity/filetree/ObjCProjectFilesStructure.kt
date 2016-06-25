package com.stanfy.helium.handler.codegen.objectivec.entity.filetree


/**
 * Created by paultaykalo on 12/17/15.
 */

class ObjCProjectFilesStructure {

  /**
   * Files those are contained in this project. Each should have an unique name
   */
  val files = arrayListOf<ObjCFile>()

  /**
   * Adds file to the project structure
   */
  fun addFile(file: ObjCFile) {
    files.add(file);
  }

  fun <T> addFiles(filesList: List<T>) where T:ObjCFile {
    files.addAll(filesList);
  }


}