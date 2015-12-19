package com.stanfy.helium.handler.codegen.objectivec.entity.classtree;

public interface  ObjCClassType {
  val name: String
}
/**
 * Created by ptaykalo on 8/17/14.
 * Simple object structure that holds information about objectiveC class
 */
public class ObjCClass(override val name: String, val definition: ObjCClassInterface,
                       val implementation: ObjCClassImplementation) : ObjCClassType {

  constructor(name:String):this(name, ObjCClassInterface(name), ObjCClassImplementation(name))
  public var forwardDeclarations = hashSetOf<String>()
    private set

  /**
   * Adds external class declaration string. This one should be transformed to "@class |externalClass|" in the eneratir
   */
  public fun addForwardDeclaration(externalClass: String) {
    forwardDeclarations.add(externalClass)
  }

}


public class ObjCPregeneratedClass(override val name:String, val header:String?, val implementation:String?) :ObjCClassType {

}

