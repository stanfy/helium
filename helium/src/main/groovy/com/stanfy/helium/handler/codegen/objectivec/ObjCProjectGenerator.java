package com.stanfy.helium.handler.codegen.objectivec;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by ptaykalo on 8/17/14
 * Generates files, based on pased in ObjCProject.
 */
public class ObjCProjectGenerator {

  /** Structure of the objC project. */
  private final ObjCProject project;

  /** Directory, where result files should be saved to*/
  private final File output;

  public ObjCProjectGenerator(final File output, final ObjCProject project) {
    this.project = project;
    this.output = output;
  }

  public void generate() {
    for (ObjCFile file : project.getFiles()) {
      File classFile = new File(output, file.getName() + "." + file.getExtension());
      Writer output = null;
      try {
        output = new OutputStreamWriter(new FileOutputStream(classFile), "UTF-8");
        output.write(file.asString());
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        IOUtils.closeQuietly(output);
      }
    }
  }
}
