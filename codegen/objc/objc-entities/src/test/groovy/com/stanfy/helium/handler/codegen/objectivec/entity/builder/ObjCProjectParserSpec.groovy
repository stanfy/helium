package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassesStructure
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCProjectFilesStructure
import com.stanfy.helium.internal.dsl.ProjectDsl
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectParserSpec extends Specification{

  ObjCDefaultClassStructureBuilder classBuilder;
  ObjCDefaultFileStructureBuilder fileBuilder;
  ProjectDsl project;
  ObjCProjectClassesStructure objCClassStructure;
  ObjCProjectFilesStructure objCFileStructure

  def setup() {
    project = new ProjectDsl()
    project.type "A" message { }
    project.type "B" message { }
    project.type "C" message { }
    classBuilder = new ObjCDefaultClassStructureBuilder(new ObjCTypeTransformer(), new ObjCPropertyNameTransformer())
    fileBuilder = new ObjCDefaultFileStructureBuilder()
  }

  def "should generate ObjCProject"() {
    when:
    objCClassStructure = classBuilder.build(project, null);

    then:
    objCClassStructure != null
  }

  def "should add ObjCFiles for each message"() {
    when:
    objCClassStructure = classBuilder.build(project, null);
    objCFileStructure = fileBuilder.build(objCClassStructure, null);

    // At least 6 files
    then:
    objCFileStructure.getFiles() != null
    objCFileStructure.getFiles().size() >= 6
  }

  def "should generate ObjCProject with .h and .m file for each message"() {
    when:
    objCClassStructure = classBuilder.build(project, null);
    objCFileStructure = fileBuilder.build(objCClassStructure, null);

    then:
    objCFileStructure.getFiles() != null
    objCFileStructure.getFiles().any({ file -> file.extension == "h" })
    objCFileStructure.getFiles().any({ file -> file.extension == "m" })
  }

  def "should generate ObjCProject with files those have message name in their names"() {
    when:
    objCClassStructure = classBuilder.build(project, null);
    objCFileStructure = fileBuilder.build(objCClassStructure, null);

    then:
    objCFileStructure.getFiles() != null
    objCFileStructure.getFiles().any({ file -> file.name.toLowerCase().contains("A".toLowerCase()) })
    objCFileStructure.getFiles().any({ file -> file.name.toLowerCase().contains("B".toLowerCase()) })
    objCFileStructure.getFiles().any({ file -> file.name.toLowerCase().contains("C".toLowerCase()) })
  }

  def "should generate ObjCProject with ,m files which have correct class implementations"() {
    when:
    objCClassStructure = classBuilder.build(project, null);
    objCFileStructure = fileBuilder.build(objCClassStructure, null);

    then:
    objCFileStructure.getFiles() != null
    objCFileStructure.getFiles().any({ file -> file.name.toLowerCase().contains("A".toLowerCase()) })
    objCFileStructure.getFiles().any({ file -> file.name.toLowerCase().contains("B".toLowerCase()) })
    objCFileStructure.getFiles().any({ file -> file.name.toLowerCase().contains("C".toLowerCase()) })
  }

// TODO : Update test
//  def "should generate ,m files wich should contain implementation part"() {
//    when:
//    classBuilder = new DefaultObjCProjectBuilder()
//    objCClassStructure = classBuilder.build(project, null);
//    def implementationFiles = objCClassStructure.getFiles().findResults({ file -> return file instanceof ObjCImplementationFile ? file : null })
//    def definitionFiles = objCClassStructure.getFiles().findResults({ file -> return file instanceof ObjCHeaderFile ? file : null })
//    then:
//    implementationFiles.every ({ file -> file.getSourceParts().any{ sourcePart -> sourcePart instanceof ObjCImplementationFileSourcePart}})
//    definitionFiles.every ({ file -> file.getSourceParts().any{ sourcePart -> sourcePart instanceof ObjCClassInterface}})
//  }

  def "should generate Classes each of those have definition and implementation"() {
    when:
    objCClassStructure = classBuilder.build(project, null);

    then:
    objCClassStructure.getClasses() != null
    objCClassStructure.getClasses().every({ objCClass -> objCClass.getDefinition() != null && objCClass.getImplementation() != null });
  }

  def "should generate Classes each of those have definition and implementation with correct names"() {
    when:
    objCClassStructure = classBuilder.build(project, null);

    then:
    objCClassStructure.getClasses() != null
    objCClassStructure.getClasses().every({ objCClass -> objCClass.getDefinition().getClassName() == objCClass.getName() && objCClass.getImplementation().getFilename() == objCClass.getName()});
  }

  def "should generate register types for all messages in the project"() {
    when:
    objCClassStructure = classBuilder.build(project, null);

    then:
    classBuilder.getTypeTransformer().objCType(project.getTypes().byName("A")) != null
    classBuilder.getTypeTransformer().objCType(project.getTypes().byName("B")) != null
    classBuilder.getTypeTransformer().objCType(project.getTypes().byName("C")) != null
  }


}
