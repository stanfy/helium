package com.stanfy.helium.handler.codegen.objectivec.generator

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.codegen.objectivec.ObjCProjectGenerator
import com.stanfy.helium.handler.codegen.objectivec.ObjcEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.builder.DefaultObjCProjectBuilder
import com.stanfy.helium.model.Type
import org.apache.commons.io.FileUtils

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCDefaultProjectGeneratorSpec extends ObjCProjectGeneratorSpec<ObjCProjectGenerator> {

  ProjectDsl projectDSL;
  DefaultObjCProjectBuilder parser;

  def setup() {

    projectDSL = new ProjectDsl()

    projectDSL.typeResolver.registerNewType( new Type(name:"string"));

    when:
    projectDSL.type "A" message {
      name 'string'
    };
    projectDSL.type "B" message { }
    projectDSL.type "C" message { }

    // Parser
    parser = new DefaultObjCProjectBuilder();
    this.project = parser.build(projectDSL);

    generator = new ObjCProjectGenerator(output, this.project);
  }

  def "should generate implementation parts"() {
    when:
    generator.generate()

    def fileAHeader = new File("$output/A.h")
    def fileAHeaderContents = FileUtils.readFileToString(fileAHeader)
    def fileAImpl = new File("$output/A.m")
    def fileAImplContents = FileUtils.readFileToString(fileAImpl)

    then:
    fileAImplContents.contains("@implementation A")
    fileAHeaderContents.contains("@interface A")
  }

  def "should generate implementation parts with prefixes from options"() {
    when:
    def options = new ObjcEntitiesOptions()
    this.project = parser.build(projectDSL, options);
    generator = new ObjCProjectGenerator(output, this.project);

    generator.generate();

    def fileAHeader = new File("$output/" + options.prefix +"A.h")
    def fileAHeaderContents = FileUtils.readFileToString(fileAHeader)
    def fileAImpl = new File("$output/" + options.prefix +"A.m")
    def fileAImplContents = FileUtils.readFileToString(fileAImpl)

    then:
    fileAImplContents.contains("@implementation " + options.prefix +"A")
    fileAHeaderContents.contains("@interface " + options.prefix +"A")
  }

}
