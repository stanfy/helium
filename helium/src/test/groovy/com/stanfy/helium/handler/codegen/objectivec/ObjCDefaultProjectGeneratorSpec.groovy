package com.stanfy.helium.handler.codegen.objectivec

import com.stanfy.helium.dsl.ProjectDsl
import com.stanfy.helium.handler.codegen.objectivec.parser.ObjCProjectParser
import com.stanfy.helium.handler.codegen.objectivec.parser.options.DefaultObjCProjectParserOptions
import org.apache.commons.io.FileUtils


/**
 * Created by ptaykalo on 8/17/14.
 */
class ObjCDefaultProjectGeneratorSpec extends ObjCProjectGeneratorSpec<ObjCProjectGenerator> {

    ProjectDsl projectDSL;
    ObjCProjectParser parser;

    def setup() {

        projectDSL = new ProjectDsl()
        projectDSL.type "A" message { }
        projectDSL.type "B" message { }
        projectDSL.type "C" message { }

        // Parser
        parser = new ObjCProjectParser();
        this.project = parser.parse(projectDSL);

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
        def options = new DefaultObjCProjectParserOptions()
        this.project = parser.parse(projectDSL, options);
        generator = new ObjCProjectGenerator(output, this.project);

        generator.generate();

        def fileAHeader = new File("$output/"+options.prefix +"A.h")
        def fileAHeaderContents = FileUtils.readFileToString(fileAHeader)
        def fileAImpl = new File("$output/"+options.prefix +"A.m")
        def fileAImplContents = FileUtils.readFileToString(fileAImpl)

        then:
        fileAImplContents.contains("@implementation "+options.prefix +"A")
        fileAHeaderContents.contains("@interface "+options.prefix +"A")
    }


}
