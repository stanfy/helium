package com.stanfy.helium.handler.codegen.objectivec.entity.classtree;

interface IObjCClass {
  val name: String
}
/**
 * Created by ptaykalo on 8/17/14.
 * Simple object structure that holds information about objectiveC class
 */
class ObjCComplexClass(override val name: String,
                       val definition: ObjCClassInterface,
                       val implementation: ObjCClassImplementation) : IObjCClass {

  constructor(name:String):this(name, ObjCClassInterface(name), ObjCClassImplementation(name))

  val classesForwardDeclarations = hashSetOf<String>()
  val protocolsForwardDeclarations = hashSetOf<String>()

  /**
   * Adds external class declaration string. This one should be transformed to "@class |externalClass|" in the source
   */
  fun addClassForwardDeclaration(externalClass: String) {
    classesForwardDeclarations.add(externalClass)
  }
  fun addClassForwardDeclarations(externalClass: List<String>) {
    classesForwardDeclarations.addAll(externalClass)
  }

  /**
   * Adds external protocol declaration string. This one should be transformed to "@protocl |externalProtocol|" in the source
   */
  fun addProtocolForwardDeclaration(externalProtocol: String) {
    protocolsForwardDeclarations.add(externalProtocol)
  }


}

class ObjCPregeneratedClass(override val name:String, val header:String?, val implementation:String?) : IObjCClass {

}

