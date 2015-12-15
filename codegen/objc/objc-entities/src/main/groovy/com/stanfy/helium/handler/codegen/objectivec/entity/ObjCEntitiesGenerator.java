package com.stanfy.helium.handler.codegen.objectivec.entity;

import com.stanfy.helium.handler.Handler;
import com.stanfy.helium.handler.codegen.BaseGenerator;
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.DefaultObjCProjectBuilder;
import com.stanfy.helium.handler.codegen.objectivec.entity.mapper.ObjCMapper;
import com.stanfy.helium.handler.codegen.objectivec.entity.mapper.sfobjectmapping.ObjCSFObjectMapper;
import com.stanfy.helium.model.Project;

import java.io.File;

/**
 * Created by ptaykalo on 8/25/14.
 */
public class ObjCEntitiesGenerator extends BaseGenerator<ObjcEntitiesOptions> implements Handler {

  private DefaultObjCProjectBuilder projectBuilder;
  private ObjCMapper mapper;

  public ObjCEntitiesGenerator(final File outputFile, final ObjcEntitiesOptions options) {
    super(outputFile, options);
    this.projectBuilder = new DefaultObjCProjectBuilder();
    this.mapper = new ObjCSFObjectMapper(); // TODO create class from (class name?)
  }

  @Override
  public void handle(final Project project) {
    ObjCProject objCProject = this.projectBuilder.build(project, getOptions());
    mapper.generateMappings(objCProject, project, getOptions());
    new ObjCProjectGenerator(getOutputDirectory(), objCProject).generate();
  }

}
