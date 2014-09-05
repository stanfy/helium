package com.stanfy.helium.handler.codegen.objectivec.parser;

import com.stanfy.helium.handler.codegen.objectivec.ObjCProject;
import com.stanfy.helium.handler.codegen.objectivec.parser.options.ObjCProjectParserOptions;
import com.stanfy.helium.model.Project;

/**
 * Created by ptaykalo on 9/2/14.
 * Interface for the class that can generate valid ObjCProject structure, based on provided
 * Helium DSL Project description
 */
public interface ObjCProjectParser {
  /*
  Returns type transformer, which is responsible for convertation
  Of types, described in Porject DSL to ObjC types
   */
  ObjCTypeTransformer getTypeTransformer();

  ObjCPropertyNameTransformer getNameTransformer();

  /*
    Performs parsing / translation of Helium DSL Proejct Structure to Objective-C Project structure
     */
  ObjCProject parse(final Project project);

  /*
    Performs parsing / translation of Helium DSL Proejct Structure to Objective-C Project structure
    Uses specified options for the generation @see ObjCProjectParserOptions
     */
  ObjCProject parse(final Project project, final ObjCProjectParserOptions options);
}
