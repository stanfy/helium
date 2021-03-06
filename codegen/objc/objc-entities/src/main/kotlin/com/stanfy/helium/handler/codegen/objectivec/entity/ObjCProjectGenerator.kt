package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCProjectFilesStructure
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.Writer

/**
 * Created by ptaykalo on 8/17/14
 * Generates files, based on based in ObjCProject.
 */
class ObjCProjectGenerator(val output: File, val filesStructure: ObjCProjectFilesStructure) {

  fun generate() {
    for (file in filesStructure.files) {
      val classFile = File(output, file.name + "." + file.getExtension())
      var output: Writer? = null
      try {
        output = OutputStreamWriter(FileOutputStream(classFile), "UTF-8");
        output.write(file.contents);
      } catch (e: RuntimeException) {
        throw RuntimeException(e);
      } finally {
        IOUtils.closeQuietly(output);
      }
    }
  }
}
