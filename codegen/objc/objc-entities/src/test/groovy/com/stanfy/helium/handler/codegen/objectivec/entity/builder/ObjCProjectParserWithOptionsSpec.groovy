package com.stanfy.helium.handler.codegen.objectivec.entity.builder

import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassesTree
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.AccessModifier
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCProjectFilesStructure
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Type
import spock.lang.Specification

/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectParserWithOptionsSpec extends Specification {

  ObjCDefaultClassStructureBuilder classStructureBuilder;
  ObjCDefaultFileStructureBuilder fileStructureBuilder;
  ProjectDsl project;
  ObjCProjectClassesTree classStructure;
  ObjCProjectFilesStructure fileStructure;
  ObjCEntitiesOptions builderOptions;

  def setup() {
    project = new ProjectDsl()
    project.type "A" message {}
    project.type "B" message {}
    project.type "C" message {
      a "A"
      b "B" sequence
    }

    builderOptions = new ObjCEntitiesOptions();
    classStructureBuilder = new ObjCDefaultClassStructureBuilder()
    fileStructureBuilder = new ObjCDefaultFileStructureBuilder()
  }

  def "should generate ObjCProject with options"() {
    when:
    classStructure = classStructureBuilder.build(project, builderOptions);

    then:
    classStructure != null
  }

  def "should add ObjCFiles for each message"() {
    when:
    classStructure = classStructureBuilder.build(project, builderOptions);
    fileStructure = fileStructureBuilder.build(classStructure, builderOptions)

    // At least 6 files
    then:
    fileStructure.getFiles() != null
    fileStructure.getFiles().size() >= 6
  }

  def "should generate ObjCProject with .h and .m file for each message"() {
    when:
    classStructure = classStructureBuilder.build(project, builderOptions);
    fileStructure = fileStructureBuilder.build(classStructure, builderOptions)

    then:
    fileStructure.getFiles() != null
    fileStructure.getFiles().any({ file -> file.extension == "h" })
    fileStructure.getFiles().any({ file -> file.extension == "m" })
  }

  def "should generate ObjCProject with files those have message name in their names and prefix from options"() {
    when:
    classStructure = classStructureBuilder.build(project, builderOptions);
    fileStructure = fileStructureBuilder.build(classStructure, builderOptions)

    then:
    fileStructure.getFiles() != null
    fileStructure.getFiles().any({ file -> file.name.toLowerCase().contains("A".toLowerCase()) && file.name.startsWith(builderOptions.getPrefix()) })
    fileStructure.getFiles().any({ file -> file.name.toLowerCase().contains("B".toLowerCase()) && file.name.startsWith(builderOptions.getPrefix()) })
    fileStructure.getFiles().any({ file -> file.name.toLowerCase().contains("C".toLowerCase()) && file.name.startsWith(builderOptions.getPrefix()) })
  }
// TODO: Update this test
//  def "should generate ,m files wich should contain implementation part"() {
//    when:
//    classBuilder = new DefaultObjCProjectBuilder()
//    objCClassStructure = classBuilder.build(project, builderOptions);
//    def implementationFiles = objCClassStructure.fileStructue..getFiles().findResults({ file -> return file instanceof ObjCImplementationFile ? file : null })
//    def definitionFiles = objCClassStructure.fileStructue..getFiles().findResults({ file -> return file instanceof ObjCHeaderFile ? file : null })
//    then:
//    implementationFiles.every ({ file -> file.getSourceParts().any{ sourcePart -> sourcePart instanceof ObjCImplementationFileSourcePart}})
//    definitionFiles.every ({ file -> file.getSourceParts().any{ sourcePart -> sourcePart instanceof ObjCClassInterface}})
//  }

  def "should generate Classes each of those have definition and implementation"() {
    when:
    classStructure = classStructureBuilder.build(project, builderOptions);

    then:
    classStructure.getClasses() != null
    classStructure.getClasses().every({ objCClass -> objCClass.getDefinition() != null && objCClass.getImplementation() != null });
  }

  def "should generate Classes each of those have definition and implementation with correct names"() {
    when:
    classStructure = classStructureBuilder.build(project, builderOptions);

    then:
    classStructure.getClasses() != null
    classStructure.getClasses().every({ objCClass -> objCClass.getDefinition().getClassName() == objCClass.getName() && objCClass.getImplementation().getFilename() == objCClass.getName() });
  }

  def "should generate register types for all messages in the project"(String message) {
    given:
    classStructure = classStructureBuilder.build(project, builderOptions);

    expect:
    classStructureBuilder.getTypeTransformer().objCType(project.getTypes().byName(message)).name == builderOptions.prefix + message;
    classStructureBuilder.getTypeTransformer().objCType(project.getTypes().byName(message)).isReference;

    where:
    message << ["A", "B", "C"]
  }

  def "should fill external classes declarations"() {
    when:
    classStructure = classStructureBuilder.build(project, builderOptions);

    def cClass = classStructure.getClasses().find { it.getDefinition().getClassName().contains("C") }

    then:
    !cClass.getMethodsForwardDeclarations.contains(builderOptions.prefix + "B") // Sequence
    cClass.getMethodsForwardDeclarations.contains(builderOptions.prefix + "A")
    !cClass.getMethodsForwardDeclarations.contains(builderOptions.prefix + "C")

  }

  def "should not generate properties with names of reserved keywords"() {
    when:
    project = new ProjectDsl()
    project.typeResolver.registerNewType(new Type(name: "int32"));

    project.type "A" message {
      copy "int32"
      copyField "int32"
      copyField1 "int32"
    }

    classStructure = classStructureBuilder.build(project, builderOptions);

    def propertyNames = classStructure.getClasses().get(0).getDefinition().getPropertyDefinitions().collect { p -> p.getName() }

    then:
    propertyNames.size() == 3
    propertyNames.toSet().size() == 3

  }

  def "should use provided custom types mappings for unknown object types"() {
    when:
    project = new ProjectDsl()
    project.typeResolver.registerNewType(new Type(name: "int32"));


    project.type "A" spec {
    }

    project.type "B" message {
      testA "A"
    }

    def customTypesMappings = new HashMap<>()
    customTypesMappings["A"] = "NSDate *"
    builderOptions.customTypesMappings = customTypesMappings;

    classStructure = classStructureBuilder.build(project, builderOptions);

    def propertyDefinitions = classStructure.getClasses().get(0).getDefinition().getPropertyDefinitions()

    then:
    classStructure.getClasses().size() == 1
    propertyDefinitions.size() == 1
    propertyDefinitions.get(0).type.name == "NSDate"
    propertyDefinitions.get(0).type.isReference
    propertyDefinitions.get(0).accessModifier == AccessModifier.STRONG

  }


  def "should use provided custom types mappings for unknown primitive types"() {
    when:
    project = new ProjectDsl()
    project.typeResolver.registerNewType(new Type(name: "int32"));


    project.type "A" spec {
    }

    project.type "B" message {
      testA "A"
    }

    def customTypesMappings = new HashMap<>()
    customTypesMappings["A"] = "somePrimitive"
    builderOptions.customTypesMappings = customTypesMappings;

    classStructure = classStructureBuilder.build(project, builderOptions);
    def propertyDefinitions = classStructure.getClasses().get(0).getDefinition().getPropertyDefinitions()

    then:
    classStructure.getClasses().size() == 1
    propertyDefinitions.size() == 1
    propertyDefinitions.get(0).type.name == "somePrimitive"
    !propertyDefinitions.get(0).type.reference
    propertyDefinitions.get(0).accessModifier == AccessModifier.ASSIGN

  }

}
