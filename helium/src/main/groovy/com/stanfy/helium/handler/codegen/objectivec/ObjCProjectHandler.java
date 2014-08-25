package com.stanfy.helium.handler.codegen.objectivec;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.objectivec.parser.ObjCProjectParser;
import com.stanfy.helium.handler.codegen.objectivec.parser.options.DefaultObjCProjectParserOptions;
import com.stanfy.helium.handler.codegen.objectivec.parser.options.ObjCProjectParserOptions;
import com.stanfy.helium.model.Project;

import java.io.File;

/**
 * Created by ptaykalo on 8/25/14.
 */
public class ObjCProjectHandler implements Handler {

  private File outputFile;
  private ObjCProjectParser projectParser;
  private ObjCProjectParserOptions projectParserOptions;
  public ObjCProjectHandler(final File outputFile, final ObjCProjectParserOptions projectParserOptions) {
    this.outputFile = outputFile;
    this.projectParserOptions = projectParserOptions;
    this.projectParser = new ObjCProjectParser();

  }

  @Override
  public void handle(final Project project) {
    ObjCProject objCProject = this.projectParser.parse(project, projectParserOptions);
    new ObjCProjectGenerator(outputFile, objCProject).generate();
  }
}
