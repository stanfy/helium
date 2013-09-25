package com.stanfy.helium.model

/**
 * Spec project.
 */
interface Project {

  /** @return types resolver */
  TypeResolver getTypes()

  /** @return services described in this project */
  List<Service> getServices()

  /** @return messages described in this project */
  List<Service> getMessages()

  /** @return notes described in this project */
  List<Note> getNotes()

  /** @return structure of the specification */
  List<StructureUnit> getStructure()

}
