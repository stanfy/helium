package com.stanfy.helium.handler.codegen.objectivec.entity.generator

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProjectGenerator
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCDefaultClassStructureBuilder
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCDefaultFileStructureBuilder
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCDefaultProjectBuilder
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassesTree
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCProjectFilesStructure
import com.stanfy.helium.handler.codegen.objectivec.entity.mapper.sfobjectmapping.ObjCSFObjectMappingsGenerator
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Type
import org.apache.commons.io.FileUtils

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCDefaultProjectGeneratorSpec extends ObjCProjectGeneratorSpec<ObjCProjectGenerator> {

  ProjectDsl projectDSL;
  ObjCDefaultClassStructureBuilder classStructureBuilder;
  ObjCDefaultFileStructureBuilder fileStructureBuilder;
  ObjCDefaultProjectBuilder projectBuilder;
  ObjCProjectClassesTree classStructure
  ObjCProjectFilesStructure filesStructure

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
    classStructureBuilder = new ObjCDefaultClassStructureBuilder();
    fileStructureBuilder = new ObjCDefaultFileStructureBuilder();
    classStructure = classStructureBuilder.build(projectDSL);
    filesStructure = fileStructureBuilder.build(classStructure)
    generator = new ObjCProjectGenerator(output, filesStructure);
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
    def options = new ObjCEntitiesOptions()
    classStructure = classStructureBuilder.build(projectDSL, options);
    filesStructure = fileStructureBuilder.build(classStructure)
    generator = new ObjCProjectGenerator(output, filesStructure);
    generator.generate();

    def fileAHeader = new File("$output/" + options.prefix +"A.h")
    def fileAHeaderContents = FileUtils.readFileToString(fileAHeader)
    def fileAImpl = new File("$output/" + options.prefix +"A.m")
    def fileAImplContents = FileUtils.readFileToString(fileAImpl)

    then:
    fileAImplContents.contains("@implementation " + options.prefix +"A")
    fileAHeaderContents.contains("@interface " + options.prefix +"A")
  }


  def "should generate mappings parts with prefixes from options"() {
    when:
    def options = new ObjCEntitiesOptions()
    classStructure = classStructureBuilder.build(projectDSL, options);
    def mapper = new ObjCSFObjectMappingsGenerator();
    projectBuilder = new ObjCDefaultProjectBuilder()
    def theProject = projectBuilder.build(projectDSL, options)
    mapper.generate(theProject, projectDSL, options);
    filesStructure = fileStructureBuilder.build(theProject.getClassesTree)
    generator = new ObjCProjectGenerator(output, filesStructure);
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
