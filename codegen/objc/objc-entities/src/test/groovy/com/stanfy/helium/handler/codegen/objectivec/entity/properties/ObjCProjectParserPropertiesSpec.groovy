package com.stanfy.helium.handler.codegen.objectivec.entity.properties
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCEntitiesOptions
import com.stanfy.helium.handler.codegen.objectivec.entity.ObjCProject
import com.stanfy.helium.handler.codegen.objectivec.entity.builder.DefaultObjCProjectBuilder
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.AccessModifier
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.AtomicModifier
import com.stanfy.helium.handler.codegen.objectivec.entity.filetree.ObjCClass
import com.stanfy.helium.internal.dsl.ProjectDsl
import com.stanfy.helium.model.Type
import spock.lang.Specification
/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCProjectParserPropertiesSpec extends Specification {

  DefaultObjCProjectBuilder parser;
  ProjectDsl project;
  ObjCProject objCProject
  ObjCEntitiesOptions options

  def setup() {
    project = new ProjectDsl()
    parser = new DefaultObjCProjectBuilder()
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
    objCProject = parser.build(project, options);

    then:
    objCProject != null
    objCProject.classStructure.getClasses().size() == 1
  }

  def "should generate ObjCProject with class and property"() {
    given:
    project.typeResolver.registerNewType( new Type(name:"string"));

    when:
    project.type "A" message {
      name 'string'
    };
    objCProject = parser.build(project, options);
    ObjCClass aClass = objCProject.classStructure.getClasses().get(0);

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
    objCProject = parser.build(project, options);
    ObjCClass aClass = objCProject.classStructure.getClasses().get(0);

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
    objCProject = parser.build(project, options);
    ObjCClass aClass = objCProject.classStructure.getClasses().get(0);

    then:
    aClass.definition != null
    aClass.definition.propertyDefinitions.size() == 1
    aClass.definition.propertyDefinitions.get(0).getType().equals("NSString *");
  }

  def "should generate string properties with NSString type and copy modifier"() {
    given:
    project.typeResolver.registerNewType( new Type(name:"string"));

    when:
    project.type "A" message {
      name 'string'
    };
    objCProject = parser.build(project, options);
    ObjCClass aClass = objCProject.classStructure.getClasses().get(0);

    then:
    aClass.definition != null
    aClass.definition.propertyDefinitions.size() == 1
    // TODO - move to the options ?
    aClass.definition.propertyDefinitions.get(0).accessModifier == AccessModifier.COPY;
  }

}
