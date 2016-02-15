package com.stanfy.helium.handler.codegen.objectivec.entity.classtree;

public interface IObjCClass {
  val name: String
}
/**
 * Created by ptaykalo on 8/17/14.
 * Simple object structure that holds information about objectiveC class
 */
public class ObjCComplexClass(override val name: String,
                              val definition: ObjCClassInterface,
                              val implementation: ObjCClassImplementation) : IObjCClass {

  constructor(name:String):this(name, ObjCClassInterface(name), ObjCClassImplementation(name))

  public val classesForwardDeclarations = hashSetOf<String>()
  public val protocolsForwardDeclarations = hashSetOf<String>()

  /**
   * Adds external class declaration string. This one should be transformed to "@class |externalClass|" in the source
   */
  public fun addClassForwardDeclaration(externalClass: String) {
    classesForwardDeclarations.add(externalClass)
  }
  public fun addClassForwardDeclarations(externalClass: List<String>) {
    classesForwardDeclarations.addAll(externalClass)
  }

  /**
   * Adds external protocol declaration string. This one should be transformed to "@protocl |externalProtocol|" in the source
   */
  public fun addProtocolForwardDeclaration(externalProtocol: String) {
    protocolsForwardDeclarations.add(externalProtocol)
  }


}

public class ObjCPregeneratedClass(override val name:String, val header:String?, val implementation:String?) : IObjCClass {

}

