package com.stanfy.helium.handler.codegen.objectivec;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.BaseGenerator;
import com.stanfy.helium.handler.codegen.objectivec.builder.DefaultObjCProjectBuilder;
import com.stanfy.helium.model.Project;

import java.io.File;

/**
 * Created by ptaykalo on 8/25/14.
 */
public class ObjCEntitiesGenerator extends BaseGenerator<ObjcEntitiesOptions> implements Handler {

  private DefaultObjCProjectBuilder projectBuilder;

  public ObjCEntitiesGenerator(final File outputFile, final ObjcEntitiesOptions options) {
    super(outputFile, options);
    this.projectBuilder = new DefaultObjCProjectBuilder();
  }

  @Override
  public void handle(final Project project) {
    ObjCProject objCProject = this.projectBuilder.build(project, getOptions());
    new ObjCProjectGenerator(getOutputDirectory(), objCProject).generate();
  }

}
