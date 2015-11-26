package com.stanfy.helium.handler.codegen.objectivec.entity.file

import spock.lang.Specification

/**
 * Spec for ObjCClassImplementation.
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
