package com.stanfy.helium.model;

import java.io.File;
import java.util.List;

/**
 * Spec project.
 */
public interface Project {
  /**
   * @return types resolver
   */
  TypeResolver getTypes();

  /**
   * @return services described in this project
   */
  List<Service> getServices();

  /**
   * @return messages described in this project
   */
  List<Message> getMessages();

  /**
   * @return notes described in this project
   */
  List<Note> getNotes();

  /**
   * @return structure of the specification
   */
  List<StructureUnit> getStructure();

  /**
   * @return sequences described in this project
   */
  List<Sequence> getSequences();

  Service serviceByName(final String name);

  /**
   * @return list of included files
   */
  List<File> getIncludedFiles();

}
