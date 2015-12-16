package com.stanfy.helium.handler.codegen.objectivec.entity.file

import spock.lang.Specification

/**
 * Spec for ObjCImplementationFileSourcePart.
 */
class ObjCClassImplementationSpec extends Specification {

  private ObjCImplementationFileSourcePart impl

  def setup() {
    impl = new ObjCImplementationFileSourcePart("TestClass")
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
