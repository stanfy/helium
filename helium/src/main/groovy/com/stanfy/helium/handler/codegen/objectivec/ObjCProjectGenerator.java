package com.stanfy.helium.handler.codegen.objectivec;

import java.io.File;

/**
 * Created by ptaykalo on 8/17/14
 * Generates files, based on pased in ObjCProject.
 */
public class ObjCProjectGenerator {

  /*
  Structure of the objC project
   */
  private final ObjCProject project;

  /*
  Directory, where result files should be saved to
   */
  private final File output;


  public ObjCProjectGenerator(final File output, final ObjCProject project) {
    this.project = project;
    this.output = output;
  }

  public void generate() {

  }
}
