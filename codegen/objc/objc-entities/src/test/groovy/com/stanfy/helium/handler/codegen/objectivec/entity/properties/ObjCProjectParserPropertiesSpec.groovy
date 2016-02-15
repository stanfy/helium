package com.stanfy.helium.handler.codegen.objectivec.entity.properties
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCDefaultClassStructureBuilder
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCPropertyNameTransformer
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.ObjCTypeTransformer
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCProjectClassesStructure
import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCComplexClass
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Type
import spock.lang.Specification
/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectParserPropertiesSpec extends Specification {

  ObjCDefaultClassStructureBuilder sut;
  ProjectDsl project;
  ObjCProjectClassesStructure classStructure
  ObjCEntitiesOptions options

  def setup() {
    project = new ProjectDsl()
    sut = new ObjCDefaultClassStructureBuilder(new ObjCTypeTransformer(), new ObjCPropertyNameTransformer())
    options = new ObjCEntitiesOptions();
  }

  //        project.type "A" message { }

  def "should generate ObjCProject with class"() {
    given:
    project.typeResolver.registerNewType( new Type(name:"string"));

    when:
    project.type "A" message {
      name 'string'
    };
    classStructure = sut.build(project, options);

    then:
    classStructure != null
    classStructure.getClasses().size() == 1
  }

  def "should generate ObjCProject with class and property"() {
    given:
    project.typeResolver.registerNewType( new Type(name:"string"));

    when:
    project.type "A" message {
      name 'string'
    };
    classStructure = sut.build(project, options);
    ObjCComplexClass aClass = classStructure.getClasses().get(0);

    then:
    aClass.definition != null
    aClass.definition.propertyDefinitions.size() == 1
  }

  def "should generate properties with default nonatomic"() {
    given:
    project.typeResolver.registerNewType( new Type(name:"string"));

    when:
    project.type "A" message {
      name 'string'
    };
    classStructure = sut.build(project, options);
    ObjCComplexClass aClass = classStructure.getClasses().get(0);

    then:
    aClass.definition != null
    aClass.definition.propertyDefinitions.size() == 1
    aClass.definition.propertyDefinitions.get(0).getAtomicModifier() == AtomicModifier.NONATOMIC;
  }

  def "should generate string properties with NSString type"() {
    given:
    project.typeResolver.registerNewType( new Type(name:"string"));

    when:
    project.type "A" message {
      name 'string'
    };
    classStructure = sut.build(project, options);
    ObjCComplexClass aClass = classStructure.getClasses().get(0);

    then:
    aClass.definition != null
    aClass.definition.propertyDefinitions.size() == 1
    aClass.definition.propertyDefinitions.get(0).getType().name == "NSString"
    aClass.definition.propertyDefinitions.get(0).getType().isReference
  }

  def "should generate string properties with NSString type and copy modifier"() {
    given:
    project.typeResolver.registerNewType( new Type(name:"string"));

    when:
    project.type "A" message {
      name 'string'
    };
    classStructure = sut.build(project, options);
    ObjCComplexClass aClass = classStructure.getClasses().get(0);

    then:
    aClass.definition != null
    aClass.definition.propertyDefinitions.size() == 1
    // TODO - move to the options ?
    aClass.definition.propertyDefinitions.get(0).accessModifier == AccessModifier.COPY;
  }

}
