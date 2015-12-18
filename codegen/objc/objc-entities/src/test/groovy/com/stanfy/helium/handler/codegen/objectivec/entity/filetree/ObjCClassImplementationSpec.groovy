package com.stanfy.helium.handler.codegen.objectivec.entity.filetree

import com.stanfy.helium.handler.codegen.objectivec.entity.classtree.ObjCClassImplementation
import spock.lang.Specification

/**
 * Spec for ObjCImplementationFileSourcePart.
 */
class ObjCClassImplementationSpec extends Specification {

  private ObjCClassImplementation impl

  def setup() {
    impl = new ObjCClassImplementation("TestClass")
  }

  def "serialized correctly"() {
    given:
    String code = impl.asString()

    expect:
    code == """
#import "TestClass.h"
@implementation TestClass
@end
""".trim()
  }

}
